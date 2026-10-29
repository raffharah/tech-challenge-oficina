package br.com.fiap.oficina.infrastructure.http.ordemservico;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.fiap.oficina.infrastructure.TestcontainersConfig;
import com.jayway.jsonpath.JsonPath;
import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class AcompanhamentoAdminIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @MockitoBean
    Clock clock;

    @BeforeEach
    void prepararBanco() {
        jdbc.execute("truncate table ordem_servico, veiculo, cliente, servico, item_estoque restart identity cascade");
        jdbc.update("insert into cliente (nome, cpf_cnpj) values ('Maria', '52998224725')");
        jdbc.update("insert into veiculo (placa, marca, modelo, ano, cliente_id) values ('ABC1D23', 'Fiat', 'Uno', 2020, 1)");
        jdbc.update("insert into servico (nome, valor_padrao) values ('Troca de oleo', 100.00)");
    }

    private void em(String instante) {
        when(clock.instant()).thenReturn(Instant.parse(instante));
    }

    private int abrir(String instante) throws Exception {
        em(instante);
        String corpo = mvc.perform(post("/ordens-servico").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpfCnpj\":\"52998224725\",\"placa\":\"ABC1D23\"}"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.id");
    }

    private void orcar(int id, String instante) throws Exception {
        em(instante);
        mvc.perform(post("/ordens-servico/" + id + "/servicos").with(jwt())
                .contentType(MediaType.APPLICATION_JSON).content("{\"servicoId\":1}"));
        mvc.perform(post("/ordens-servico/" + id + "/orcamento").with(jwt())).andExpect(status().isOk());
    }

    private void executar(int id, String aprovacao, String finalizacao) throws Exception {
        orcar(id, aprovacao);
        em(aprovacao);
        mvc.perform(post("/cliente/ordens-servico/" + id + "/aprovacao")).andExpect(status().isOk());
        if (finalizacao != null) {
            em(finalizacao);
            mvc.perform(post("/ordens-servico/" + id + "/finalizacao").with(jwt())).andExpect(status().isOk());
        }
    }

    @Test
    void listaOrdenadaPorAberturaDecrescente() throws Exception {
        int primeira = abrir("2026-01-10T10:00:00Z");
        int segunda = abrir("2026-01-11T10:00:00Z");

        mvc.perform(get("/ordens-servico").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.itens[0].id").value(segunda))
                .andExpect(jsonPath("$.itens[1].id").value(primeira))
                .andExpect(jsonPath("$.itens[0].cpfCnpj").value("52998224725"))
                .andExpect(jsonPath("$.itens[0].placa").value("ABC1D23"))
                .andExpect(jsonPath("$.itens[0].status").value("EM_DIAGNOSTICO"));
    }

    @Test
    void detalhaOrdemComComponentesTotalEHistorico() throws Exception {
        int id = abrir("2026-01-10T10:00:00Z");
        orcar(id, "2026-01-10T11:00:00Z");

        mvc.perform(get("/ordens-servico/" + id).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpfCnpj").value("52998224725"))
                .andExpect(jsonPath("$.placa").value("ABC1D23"))
                .andExpect(jsonPath("$.servicos[0].nome").value("Troca de oleo"))
                .andExpect(jsonPath("$.total").value(100.00))
                .andExpect(jsonPath("$.historico.length()").value(3));
    }

    @Test
    void detalharInexistenteRetorna404() throws Exception {
        mvc.perform(get("/ordens-servico/999").with(jwt())).andExpect(status().isNotFound());
    }

    @Test
    void tempoMedioDeDuasOrdensFinalizadas() throws Exception {
        int a = abrir("2026-01-10T10:00:00Z");
        executar(a, "2026-01-11T09:00:00Z", "2026-01-11T10:00:00Z");
        int b = abrir("2026-01-10T10:30:00Z");
        executar(b, "2026-01-12T09:00:00Z", "2026-01-12T11:00:00Z");

        mvc.perform(get("/ordens-servico/tempo-medio-execucao").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaMinutos").value(90.0))
                .andExpect(jsonPath("$.quantidade").value(2));
    }

    @Test
    void semOrdemFinalizadaQuantidadeZeroEMediaNula() throws Exception {
        mvc.perform(get("/ordens-servico/tempo-medio-execucao").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaMinutos").doesNotExist())
                .andExpect(jsonPath("$.quantidade").value(0));
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(get("/ordens-servico")).andExpect(status().isUnauthorized());
        mvc.perform(get("/ordens-servico/1")).andExpect(status().isUnauthorized());
        mvc.perform(get("/ordens-servico/tempo-medio-execucao")).andExpect(status().isUnauthorized());
    }
}
