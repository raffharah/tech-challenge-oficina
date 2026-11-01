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
        return mvc.perform(post("/cliente/ordens-servico/" + id + "/" + decisao)
                .contentType(MediaType.APPLICATION_JSON).content("{\"cpfCnpj\":\"" + cpfCnpj + "\"}"));
    }

    private int saldoFisico() {
        return jdbc.queryForObject("select saldo_fisico from item_estoque where id = 1", Integer.class);
    }

    @Test
    void aprovacaoComSaldoBaixaEstoqueEVaiParaEmExecucao() throws Exception {
        aguardarAprovacao(4);

        decidir("aprovacao", ordemId, TITULAR)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO"));

        assertThat(saldoFisico()).isEqualTo(6);
    }

    @Test
    void aprovacaoSemSaldoRetorna422ComItensFaltantesSemAlterarNada() throws Exception {
        aguardarAprovacao(11);

        decidir("aprovacao", ordemId, TITULAR)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("SALDO_INSUFICIENTE"))
                .andExpect(jsonPath("$.detalhes.length()").value(1));

        assertThat(saldoFisico()).isEqualTo(10);
        assertThat(jdbc.queryForObject("select status from ordem_servico where id = ?", String.class, ordemId))
                .isEqualTo("AGUARDANDO_APROVACAO");
    }

    @Test
    void rejeicaoCancelaEBloqueiaInclusaoPosterior() throws Exception {
        aguardarAprovacao(2);

        decidir("rejeicao", ordemId, TITULAR)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        mvc.perform(post("/ordens-servico/" + ordemId + "/servicos").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"servicoId\":1}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void decisaoForaDeAguardandoAprovacaoRetorna422() throws Exception {
        decidir("aprovacao", ordemId, TITULAR)
                .andExpect(status().isUnprocessableEntity());
        decidir("rejeicao", ordemId, TITULAR)
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void ordemInexistenteRetorna404() throws Exception {
        decidir("aprovacao", 999, TITULAR).andExpect(status().isNotFound());
        decidir("rejeicao", 999, TITULAR).andExpect(status().isNotFound());
    }

    @Test
    void cpfDeOutroTitularRetorna404ComCorpoIdenticoSemAlterarNada() throws Exception {
        aguardarAprovacao(4);

        for (String decisao : new String[] {"aprovacao", "rejeicao"}) {
            String outroCpf = decidir(decisao, ordemId, "11144477735").andExpect(status().isNotFound())
                    .andReturn().getResponse().getContentAsString();
            String inexistente = decidir(decisao, 999, TITULAR).andExpect(status().isNotFound())
                    .andReturn().getResponse().getContentAsString();
            assertThat(outroCpf).isEqualTo(inexistente).doesNotContain(TITULAR).doesNotContain("ABC1D23");
        }

        assertThat(saldoFisico()).isEqualTo(10);
        assertThat(jdbc.queryForObject("select status from ordem_servico where id = ?", String.class, ordemId))
                .isEqualTo("AGUARDANDO_APROVACAO");
    }

    @Test
    void decisaoSemCpfRetorna400() throws Exception {
        aguardarAprovacao(2);

        mvc.perform(post("/cliente/ordens-servico/" + ordemId + "/aprovacao")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("cpfCnpj"));
        mvc.perform(post("/cliente/ordens-servico/" + ordemId + "/rejeicao"))
                .andExpect(status().isBadRequest());
    }
}
