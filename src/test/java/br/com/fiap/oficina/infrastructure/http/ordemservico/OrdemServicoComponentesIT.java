package br.com.fiap.oficina.infrastructure.http.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class OrdemServicoComponentesIT {

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

    private void incluirServico(long servicoId, int esperado) throws Exception {
        mvc.perform(post("/ordens-servico/" + ordemId + "/servicos").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"servicoId\":" + servicoId + "}"))
                .andExpect(status().is(esperado));
    }

    private void incluirItem(long itemId, int quantidade, int esperado) throws Exception {
        mvc.perform(post("/ordens-servico/" + ordemId + "/itens").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":%d,\"quantidade\":%d}".formatted(itemId, quantidade)))
                .andExpect(status().is(esperado));
    }

    @Test
    void incluiServicoComValorPadrao() throws Exception {
        mvc.perform(post("/ordens-servico/" + ordemId + "/servicos").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"servicoId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.servicos.length()").value(1))
                .andExpect(jsonPath("$.servicos[0].nome").value("Troca de oleo"))
                .andExpect(jsonPath("$.servicos[0].valor").value(100.00));
    }

    @Test
    void incluiItemComPrecoDeVendaESomaQuantidade() throws Exception {
        incluirItem(1, 2, 201);

        mvc.perform(post("/ordens-servico/" + ordemId + "/itens").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"itemId\":1,\"quantidade\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itens.length()").value(1))
                .andExpect(jsonPath("$.itens[0].sku").value("OLE-1"))
                .andExpect(jsonPath("$.itens[0].quantidade").value(5))
                .andExpect(jsonPath("$.itens[0].precoUnitario").value(35.50));
    }

    @Test
    void removeServicoEItem() throws Exception {
        incluirServico(1, 201);
        incluirItem(1, 2, 201);

        mvc.perform(delete("/ordens-servico/" + ordemId + "/servicos/1").with(jwt()))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/ordens-servico/" + ordemId + "/itens/1").with(jwt()))
                .andExpect(status().isNoContent());

        assertThat(jdbc.queryForObject("select count(*) from ordem_servico_servico", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from ordem_servico_item", Integer.class)).isZero();
    }

    @Test
    void servicoOuItemInexistenteRetorna422() throws Exception {
        incluirServico(99, 422);
        incluirItem(99, 1, 422);
    }

    @Test
    void ordemInexistenteRetorna404() throws Exception {
        mvc.perform(post("/ordens-servico/999/servicos").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"servicoId\":1}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void servicoRepetidoRetorna422SemDuplicar() throws Exception {
        incluirServico(1, 201);
        incluirServico(1, 422);

        assertThat(jdbc.queryForObject("select count(*) from ordem_servico_servico", Integer.class)).isEqualTo(1);
    }

    @Test
    void quantidadeZeroOuAusenteRetornaErro() throws Exception {
        incluirItem(1, 0, 422);
        mvc.perform(post("/ordens-servico/" + ordemId + "/itens").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"itemId\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void foraDeDiagnosticoRetorna422() throws Exception {
        incluirServico(1, 201);
        mvc.perform(post("/ordens-servico/" + ordemId + "/orcamento").with(jwt())).andExpect(status().isOk());

        incluirServico(1, 422);
        incluirItem(1, 1, 422);
        mvc.perform(delete("/ordens-servico/" + ordemId + "/servicos/1").with(jwt()))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(delete("/ordens-servico/" + ordemId + "/itens/1").with(jwt()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(post("/ordens-servico/" + ordemId + "/servicos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"servicoId\":1}"))
                .andExpect(status().isUnauthorized());
    }
}
