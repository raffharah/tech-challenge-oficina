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
class OrdemServicoOrcamentoIT {

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
        jdbc.update("insert into item_estoque (sku, nome, tipo, preco_venda, saldo_fisico) values ('OLE-1', 'Oleo', 'INSUMO', 35.50, 10)");
        String corpo = mvc.perform(post("/ordens-servico").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpfCnpj\":\"52998224725\",\"placa\":\"ABC1D23\"}"))
                .andReturn().getResponse().getContentAsString();
        ordemId = JsonPath.read(corpo, "$.id");
    }

    @Test
    void concluiDiagnosticoComTotalEAguardaAprovacao() throws Exception {
        mvc.perform(post("/ordens-servico/" + ordemId + "/servicos").with(jwt())
                .contentType(MediaType.APPLICATION_JSON).content("{\"servicoId\":1}"));
        mvc.perform(post("/ordens-servico/" + ordemId + "/itens").with(jwt())
                .contentType(MediaType.APPLICATION_JSON).content("{\"itemId\":1,\"quantidade\":2}"));

        mvc.perform(post("/ordens-servico/" + ordemId + "/orcamento").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(171.00))
                .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"))
                .andExpect(jsonPath("$.historico.length()").value(3));
    }

    @Test
    void semServicoRetorna422() throws Exception {
        mvc.perform(post("/ordens-servico/" + ordemId + "/orcamento").with(jwt()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("ORDEM_SEM_SERVICO"));
    }

    @Test
    void segundaConclusaoRetorna422() throws Exception {
        mvc.perform(post("/ordens-servico/" + ordemId + "/servicos").with(jwt())
                .contentType(MediaType.APPLICATION_JSON).content("{\"servicoId\":1}"));
        mvc.perform(post("/ordens-servico/" + ordemId + "/orcamento").with(jwt())).andExpect(status().isOk());

        mvc.perform(post("/ordens-servico/" + ordemId + "/orcamento").with(jwt()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void ordemInexistenteRetorna404() throws Exception {
        mvc.perform(post("/ordens-servico/999/orcamento").with(jwt())).andExpect(status().isNotFound());
    }
}
