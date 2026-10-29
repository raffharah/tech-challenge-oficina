package br.com.fiap.oficina.infrastructure.http.ordemservico;

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
class ExecucaoEntregaIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    private int ordemId;

    @BeforeEach
    void prepararBanco() throws Exception {
        jdbc.execute("truncate table ordem_servico, veiculo, cliente, servico, item_estoque restart identity cascade");
        jdbc.update("insert into cliente (nome, cpf_cnpj) values ('Maria', '52998224725')");
        jdbc.update("insert into veiculo (placa, marca, modelo, ano, cliente_id) values ('ABC1D23', 'Fiat', 'Uno', 2020, 1)");
        jdbc.update("insert into servico (nome, valor_padrao) values ('Troca de oleo', 100.00)");
        String corpo = mvc.perform(post("/ordens-servico").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpfCnpj\":\"52998224725\",\"placa\":\"ABC1D23\"}"))
                .andReturn().getResponse().getContentAsString();
        ordemId = JsonPath.read(corpo, "$.id");
    }

    private void aprovar() throws Exception {
        mvc.perform(post("/ordens-servico/" + ordemId + "/servicos").with(jwt())
                .contentType(MediaType.APPLICATION_JSON).content("{\"servicoId\":1}"));
        mvc.perform(post("/ordens-servico/" + ordemId + "/orcamento").with(jwt())).andExpect(status().isOk());
        mvc.perform(post("/cliente/ordens-servico/" + ordemId + "/aprovacao")).andExpect(status().isOk());
    }

    @Test
    void fluxoCompletoTerminaEntregue() throws Exception {
        aprovar();

        mvc.perform(post("/ordens-servico/" + ordemId + "/finalizacao").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINALIZADA"));
        mvc.perform(post("/ordens-servico/" + ordemId + "/entrega").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGUE"));
    }

    @Test
    void finalizarEmDiagnosticoRetorna422() throws Exception {
        mvc.perform(post("/ordens-servico/" + ordemId + "/finalizacao").with(jwt()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void entregarEmExecucaoRetorna422() throws Exception {
        aprovar();

        mvc.perform(post("/ordens-servico/" + ordemId + "/entrega").with(jwt()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(post("/ordens-servico/" + ordemId + "/finalizacao")).andExpect(status().isUnauthorized());
        mvc.perform(post("/ordens-servico/" + ordemId + "/entrega")).andExpect(status().isUnauthorized());
    }
}
