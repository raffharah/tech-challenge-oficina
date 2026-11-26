package br.com.fiap.oficina.infrastructure.gateway.notificacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.fiap.oficina.infrastructure.TestcontainersConfig;
import com.jayway.jsonpath.JsonPath;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@ExtendWith(OutputCaptureExtension.class)
class NotificacaoDeStatusImplIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PostgreSQLContainer postgres;

    @MockitoBean
    JavaMailSender mail;

    private int ordemId;

    @BeforeEach
    void prepararBanco() {
        reset(mail);
        jdbc.execute("truncate table ordem_servico, veiculo, cliente, servico, item_estoque restart identity cascade");
        jdbc.update("insert into cliente (nome, cpf_cnpj) values ('Maria', '52998224725')");
        jdbc.update("insert into veiculo (placa, marca, modelo, ano, cliente_id) values ('ABC1D23', 'Fiat', 'Uno', 2020, 1)");
        jdbc.update("insert into servico (nome, valor_padrao) values ('Troca de oleo', 100.00)");
        jdbc.update("insert into item_estoque (sku, nome, tipo, preco_venda, saldo_fisico) values ('OLE-1', 'Oleo', 'INSUMO', 35.50, 10)");
    }

    private void abrir(int quantidade) throws Exception {
        String corpo = mvc.perform(post("/ordens-servico").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpfCnpj\":\"52998224725\",\"email\":\"maria@oficina.com\",\"placa\":\"ABC1D23\","
                                + "\"servicos\":[1],\"itens\":[{\"itemId\":1,\"quantidade\":" + quantidade + "}]}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        ordemId = JsonPath.read(corpo, "$.id");
    }

    private void passo(String rota) throws Exception {
        mvc.perform(post("/ordens-servico/" + ordemId + "/" + rota).with(jwt())).andExpect(status().isOk());
    }

    private void decidir(String decisao) throws Exception {
        mvc.perform(post("/cliente/ordens-servico/" + ordemId + "/decisao").contentType(MediaType.APPLICATION_JSON)
                .content("{\"cpfCnpj\":\"52998224725\",\"decisao\":\"" + decisao + "\"}"));
    }

    private List<SimpleMailMessage> enviadas(int vezes) {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mail, times(vezes)).send(captor.capture());
        return captor.getAllValues();
    }

    @Test
    void cadaMudancaDeStatusEnviaEmailAoTitularComOStatusEOTotalNoOrcamento() throws Exception {
        abrir(1);
        passo("diagnostico");
        passo("orcamento");
        decidir("APROVADO");
        passo("finalizacao");
        passo("entrega");

        List<SimpleMailMessage> mensagens = enviadas(6);
        assertThat(mensagens).allSatisfy(m -> assertThat(m.getTo()).containsExactly("maria@oficina.com"));
        List<String> status = List.of("RECEBIDA", "EM_DIAGNOSTICO", "AGUARDANDO_APROVACAO", "EM_EXECUCAO",
                "FINALIZADA", "ENTREGUE");
        for (int i = 0; i < 6; i++) {
            assertThat(mensagens.get(i).getText()).contains("Status: " + status.get(i)).contains("ABC1D23");
        }
        assertThat(mensagens.get(2).getText()).contains("Total do orcamento: 135.50");
        assertThat(mensagens.get(3).getText()).doesNotContain("Total do orcamento");
    }

    @Test
    void saldoInsuficienteNaoEnviaEmail() throws Exception {
        abrir(11);
        passo("diagnostico");
        passo("orcamento");
        reset(mail);

        decidir("APROVADO");

        verify(mail, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void envioAcontecePosCommitComOStatusJaGravado() throws Exception {
        JdbcTemplate outraConexao = new JdbcTemplate(new DriverManagerDataSource(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword()));
        List<String> statusVistos = new ArrayList<>();
        doAnswer(i -> {
            statusVistos.add(outraConexao.queryForObject("select status from ordem_servico", String.class));
            return null;
        }).when(mail).send(any(SimpleMailMessage.class));

        abrir(1);

        assertThat(statusVistos).containsExactly("RECEBIDA");
    }

    @Test
    void falhaDoServidorDeEmailMantem200EStatusGravadoERegistraLogComONumeroDaOs(CapturedOutput saida)
            throws Exception {
        abrir(1);
        doThrow(new MailSendException("fora do ar")).when(mail).send(any(SimpleMailMessage.class));

        mvc.perform(post("/ordens-servico/" + ordemId + "/diagnostico").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_DIAGNOSTICO"));

        assertThat(jdbc.queryForObject("select status from ordem_servico", String.class))
                .isEqualTo("EM_DIAGNOSTICO");
        assertThat(saida.getOut()).contains("Falha ao enviar e-mail de status da OS " + ordemId);
    }

    @Test
    void clienteSemEmailNaoRecebeEmailEAMudancaDeStatusSegue() throws Exception {
        abrir(1);
        jdbc.update("update cliente set email = null");
        reset(mail);

        mvc.perform(post("/ordens-servico/" + ordemId + "/diagnostico").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_DIAGNOSTICO"));

        verify(mail, never()).send(any(SimpleMailMessage.class));
        assertThat(jdbc.queryForObject("select status from ordem_servico", String.class))
                .isEqualTo("EM_DIAGNOSTICO");
    }
}
