package br.com.fiap.oficina.infrastructure.http.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.fiap.oficina.infrastructure.TestcontainersConfig;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class DecisaoOrcamentoIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    private static final String TITULAR = "52998224725";

    private int ordemId;

    @BeforeEach
    void prepararBanco() throws Exception {
        jdbc.execute("truncate table ordem_servico, veiculo, cliente, servico, item_estoque restart identity cascade");
        jdbc.update("insert into cliente (nome, cpf_cnpj) values ('Maria', '52998224725')");
        jdbc.update("insert into veiculo (placa, marca, modelo, ano, cliente_id) values ('ABC1D23', 'Fiat', 'Uno', 2020, 1)");
        jdbc.update("insert into servico (nome, valor_padrao) values ('Troca de oleo', 100.00)");
        jdbc.update("insert into item_estoque (sku, nome, tipo, preco_venda, saldo_fisico) values ('OLE-1', 'Oleo', 'INSUMO', 35.50, 10)");
        String corpo = mvc.perform(post("/ordens-servico").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpfCnpj\":\"52998224725\",\"placa\":\"ABC1D23\"}"))
                .andReturn().getResponse().getContentAsString();
        ordemId = JsonPath.read(corpo, "$.id");
        mvc.perform(post("/ordens-servico/" + ordemId + "/diagnostico").with(jwt())).andExpect(status().isOk());
    }

    private void aguardarAprovacao(int quantidade) throws Exception {
        mvc.perform(post("/ordens-servico/" + ordemId + "/servicos").with(jwt())
                .contentType(MediaType.APPLICATION_JSON).content("{\"servicoId\":1}"));
        mvc.perform(post("/ordens-servico/" + ordemId + "/itens").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"itemId\":1,\"quantidade\":" + quantidade + "}"));
        mvc.perform(post("/ordens-servico/" + ordemId + "/orcamento").with(jwt())).andExpect(status().isOk());
    }

    private ResultActions decidir(String decisao, int id, String cpfCnpj) throws Exception {
        return mvc.perform(post("/cliente/ordens-servico/" + id + "/decisao")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cpfCnpj\":\"" + cpfCnpj + "\",\"decisao\":\"" + decisao + "\"}"));
    }

    private String statusDaOrdem() {
        return jdbc.queryForObject("select status from ordem_servico where id = ?", String.class, ordemId);
    }

    private int saldoFisico() {
        return jdbc.queryForObject("select saldo_fisico from item_estoque where id = 1", Integer.class);
    }

    @Test
    void aprovadoComSaldoBaixaEstoqueEVaiParaEmExecucao() throws Exception {
        aguardarAprovacao(4);

        decidir("APROVADO", ordemId, TITULAR)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO"));

        assertThat(saldoFisico()).isEqualTo(6);
    }

    @Test
    void aprovadoSemSaldoRetorna422ComItensFaltantesSemAlterarNada() throws Exception {
        aguardarAprovacao(11);

        decidir("APROVADO", ordemId, TITULAR)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("SALDO_INSUFICIENTE"))
                .andExpect(jsonPath("$.detalhes.length()").value(1));

        assertThat(saldoFisico()).isEqualTo(10);
        assertThat(statusDaOrdem()).isEqualTo("AGUARDANDO_APROVACAO");
    }

    @Test
    void recusadoCancelaSemBaixarEBloqueiaInclusaoPosterior() throws Exception {
        aguardarAprovacao(2);

        decidir("RECUSADO", ordemId, TITULAR)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        assertThat(saldoFisico()).isEqualTo(10);
        mvc.perform(post("/ordens-servico/" + ordemId + "/servicos").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"servicoId\":1}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void decisaoForaDeAguardandoAprovacaoRetorna422TransicaoInvalida() throws Exception {
        for (String decisao : new String[] {"APROVADO", "RECUSADO"}) {
            decidir(decisao, ordemId, TITULAR)
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.codigo").value("TRANSICAO_INVALIDA"));
        }
    }

    @Test
    void ordemInexistenteRetorna404() throws Exception {
        for (String decisao : new String[] {"APROVADO", "RECUSADO"}) {
            decidir(decisao, 999, TITULAR).andExpect(status().isNotFound());
        }
    }

    @Test
    void cpfDeOutroTitularRetorna404ComCorpoIdenticoSemAlterarNada() throws Exception {
        aguardarAprovacao(4);

        for (String decisao : new String[] {"APROVADO", "RECUSADO"}) {
            String outroCpf = decidir(decisao, ordemId, "11144477735").andExpect(status().isNotFound())
                    .andReturn().getResponse().getContentAsString();
            String inexistente = decidir(decisao, 999, TITULAR).andExpect(status().isNotFound())
                    .andReturn().getResponse().getContentAsString();
            assertThat(outroCpf).isEqualTo(inexistente).doesNotContain(TITULAR).doesNotContain("ABC1D23");
        }

        assertThat(saldoFisico()).isEqualTo(10);
        assertThat(statusDaOrdem()).isEqualTo("AGUARDANDO_APROVACAO");
    }

    @Test
    void corpoSemCpfOuSemDecisaoOuComDecisaoDesconhecidaRetorna400() throws Exception {
        aguardarAprovacao(2);

        for (String corpo : new String[] {"{\"decisao\":\"APROVADO\"}", "{\"cpfCnpj\":\"" + TITULAR + "\"}",
                "{\"cpfCnpj\":\"" + TITULAR + "\",\"decisao\":\"TALVEZ\"}"}) {
            mvc.perform(post("/cliente/ordens-servico/" + ordemId + "/decisao")
                            .contentType(MediaType.APPLICATION_JSON).content(corpo))
                    .andExpect(status().isBadRequest());
        }
        assertThat(statusDaOrdem()).isEqualTo("AGUARDANDO_APROVACAO");
    }

    @Test
    void cpfInvalidoRetorna422() throws Exception {
        decidir("APROVADO", ordemId, "123")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("CPF_CNPJ_INVALIDO"));
    }

    @Test
    void rotasAntigasNaoExistemMaisESeguemSemAlterarAOrdem() throws Exception {
        aguardarAprovacao(2);

        for (String rota : new String[] {"aprovacao", "rejeicao"}) {
            mvc.perform(post("/cliente/ordens-servico/" + ordemId + "/" + rota)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"cpfCnpj\":\"" + TITULAR + "\"}"))
                    .andExpect(status().isNotFound());
        }
        assertThat(statusDaOrdem()).isEqualTo("AGUARDANDO_APROVACAO");
    }
}
