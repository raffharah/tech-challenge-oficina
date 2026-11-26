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

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class OrdemServicoAberturaIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void prepararBanco() {
        jdbc.execute("truncate table ordem_servico, veiculo, cliente, servico, item_estoque restart identity cascade");
        jdbc.update("insert into cliente (nome, cpf_cnpj) values ('Maria', '52998224725')");
        jdbc.update("insert into cliente (nome, cpf_cnpj) values ('Joao', '11144477735')");
        jdbc.update("insert into veiculo (placa, marca, modelo, ano, cliente_id) values ('ABC1D23', 'Fiat', 'Uno', 2020, 1)");
        jdbc.update("insert into servico (nome, valor_padrao) values ('Troca de oleo', 100.00)");
        jdbc.update("insert into item_estoque (sku, nome, tipo, preco_venda, saldo_fisico) values ('OLE-1', 'Oleo', 'INSUMO', 35.50, 10)");
    }

    private String corpo(String cpfCnpj, String placa) {
        return corpo(cpfCnpj, "maria@oficina.com", placa, "[1]", "[{\"itemId\":1,\"quantidade\":2}]");
    }

    private String corpo(String cpfCnpj, String email, String placa, String servicos, String itens) {
        return "{\"cpfCnpj\":\"%s\",\"email\":\"%s\",\"placa\":\"%s\",\"servicos\":%s,\"itens\":%s}"
                .formatted(cpfCnpj, email, placa, servicos, itens);
    }

    private String emailDoCliente() {
        return jdbc.queryForObject("select email from cliente where id = 1", String.class);
    }

    @Test
    void abreOrdemRecebidaRetorna201() throws Exception {
        mvc.perform(post("/ordens-servico").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("529.982.247-25", "abc1d23")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andExpect(jsonPath("$.cpfCnpj").value("52998224725"))
                .andExpect(jsonPath("$.placa").value("ABC1D23"))
                .andExpect(jsonPath("$.historico.length()").value(1))
                .andExpect(jsonPath("$.historico[0].status").value("RECEBIDA"))
                .andExpect(jsonPath("$.servicos.length()").value(1))
                .andExpect(jsonPath("$.servicos[0].valor").value(100.00))
                .andExpect(jsonPath("$.itens.length()").value(1))
                .andExpect(jsonPath("$.itens[0].quantidade").value(2))
                .andExpect(jsonPath("$.itens[0].precoUnitario").value(35.50))
                .andExpect(jsonPath("$.total").doesNotExist());

        assertThat(emailDoCliente()).isEqualTo("maria@oficina.com");
        assertThat(jdbc.queryForObject("select saldo_fisico from item_estoque where id = 1", Integer.class))
                .isEqualTo(10);
    }

    @Test
    void servicoInexistenteRetorna404SemGravarOrdemNemEmail() throws Exception {
        mvc.perform(post("/ordens-servico").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("52998224725", "novo@oficina.com", "ABC1D23", "[99]",
                                "[{\"itemId\":1,\"quantidade\":1}]")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("SERVICO_NAO_ENCONTRADO"));

        assertThat(jdbc.queryForObject("select count(*) from ordem_servico", Integer.class)).isZero();
        assertThat(emailDoCliente()).isNull();
    }

    @Test
    void itemInexistenteRetorna404SemGravarOrdemNemEmail() throws Exception {
        mvc.perform(post("/ordens-servico").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("52998224725", "novo@oficina.com", "ABC1D23", "[1]",
                                "[{\"itemId\":99,\"quantidade\":1}]")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("ITEM_NAO_ENCONTRADO"));

        assertThat(jdbc.queryForObject("select count(*) from ordem_servico", Integer.class)).isZero();
        assertThat(emailDoCliente()).isNull();
    }

    @Test
    void emailInvalidoRetorna422SemGravarOrdem() throws Exception {
        mvc.perform(post("/ordens-servico").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("52998224725", "ana", "ABC1D23", "[1]",
                                "[{\"itemId\":1,\"quantidade\":1}]")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("EMAIL_INVALIDO"));

        assertThat(jdbc.queryForObject("select count(*) from ordem_servico", Integer.class)).isZero();
    }

    @Test
    void emailNovoSubstituiOAnterior() throws Exception {
        jdbc.update("update cliente set email = 'antigo@oficina.com' where id = 1");

        mvc.perform(post("/ordens-servico").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("52998224725", "ABC1D23")))
                .andExpect(status().isCreated());

        assertThat(emailDoCliente()).isEqualTo("maria@oficina.com");
    }

    @Test
    void corpoIncompletoRetorna400() throws Exception {
        for (String corpo : new String[] {
                "{\"cpfCnpj\":\"52998224725\",\"placa\":\"ABC1D23\",\"servicos\":[1],\"itens\":[{\"itemId\":1,\"quantidade\":1}]}",
                "{\"cpfCnpj\":\"52998224725\",\"email\":\"a@b.com\",\"placa\":\"ABC1D23\",\"servicos\":[],\"itens\":[{\"itemId\":1,\"quantidade\":1}]}",
                "{\"cpfCnpj\":\"52998224725\",\"email\":\"a@b.com\",\"placa\":\"ABC1D23\",\"servicos\":[1]}"}) {
            mvc.perform(post("/ordens-servico").with(jwt()).contentType(MediaType.APPLICATION_JSON).content(corpo))
                    .andExpect(status().isBadRequest());
        }
    }

    private int abrir() throws Exception {
        String corpo = mvc.perform(post("/ordens-servico").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("52998224725", "ABC1D23")))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.id");
    }

    @Test
    void iniciaDiagnosticoRetorna200ComHistorico() throws Exception {
        int id = abrir();

        mvc.perform(post("/ordens-servico/" + id + "/diagnostico").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_DIAGNOSTICO"))
                .andExpect(jsonPath("$.historico.length()").value(2))
                .andExpect(jsonPath("$.historico[1].status").value("EM_DIAGNOSTICO"));
    }

    @Test
    void iniciarDiagnosticoDuasVezesRetorna422() throws Exception {
        int id = abrir();
        mvc.perform(post("/ordens-servico/" + id + "/diagnostico").with(jwt())).andExpect(status().isOk());

        mvc.perform(post("/ordens-servico/" + id + "/diagnostico").with(jwt()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("TRANSICAO_INVALIDA"));
    }

    @Test
    void incluirServicoAntesDoDiagnosticoRetorna422() throws Exception {
        int id = abrir();

        mvc.perform(post("/ordens-servico/" + id + "/servicos").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"servicoId\":1}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("ORDEM_FORA_DE_DIAGNOSTICO"));
    }

    @Test
    void iniciarDiagnosticoDeOrdemInexistenteRetorna404() throws Exception {
        mvc.perform(post("/ordens-servico/99/diagnostico").with(jwt())).andExpect(status().isNotFound());
    }

    @Test
    void iniciarDiagnosticoSemTokenRetorna401() throws Exception {
        int id = abrir();

        mvc.perform(post("/ordens-servico/" + id + "/diagnostico")).andExpect(status().isUnauthorized());
    }

    @Test
    void cpfOuPlacaInvalidosRetorna422SemCriarOrdem() throws Exception {
        mvc.perform(post("/ordens-servico").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("123", "ABC1D23")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("CPF_CNPJ_INVALIDO"));
        mvc.perform(post("/ordens-servico").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("52998224725", "XX")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("PLACA_INVALIDA"));
        assertThat(
                jdbc.queryForObject("select count(*) from ordem_servico", Integer.class)).isZero();
    }

    @Test
    void clienteOuVeiculoInexistenteRetorna404() throws Exception {
        mvc.perform(post("/ordens-servico").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("39053344705", "ABC1D23")))
                .andExpect(status().isNotFound());
        mvc.perform(post("/ordens-servico").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("52998224725", "ZZZ9Z99")))
                .andExpect(status().isNotFound());
    }

    @Test
    void veiculoDeOutroClienteRetorna422() throws Exception {
        mvc.perform(post("/ordens-servico").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("11144477735", "ABC1D23")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("VEICULO_DE_OUTRO_CLIENTE"));
    }

    @Test
    void semCamposRetorna400() throws Exception {
        mvc.perform(post("/ordens-servico").with(jwt()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(post("/ordens-servico").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("52998224725", "ABC1D23")))
                .andExpect(status().isUnauthorized());
    }
}
