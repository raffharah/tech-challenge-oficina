package br.com.fiap.oficina.infrastructure.http.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.fiap.oficina.infrastructure.TestcontainersConfig;
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
    }

    private String corpo(String cpfCnpj, String placa) {
        return "{\"cpfCnpj\":\"%s\",\"placa\":\"%s\"}".formatted(cpfCnpj, placa);
    }

    @Test
    void abreOrdemEmDiagnosticoRetorna201() throws Exception {
        mvc.perform(post("/ordens-servico").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("529.982.247-25", "abc1d23")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("EM_DIAGNOSTICO"))
                .andExpect(jsonPath("$.cpfCnpj").value("52998224725"))
                .andExpect(jsonPath("$.placa").value("ABC1D23"))
                .andExpect(jsonPath("$.historico.length()").value(2))
                .andExpect(jsonPath("$.historico[0].status").value("RECEBIDA"))
                .andExpect(jsonPath("$.historico[1].status").value("EM_DIAGNOSTICO"));
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
