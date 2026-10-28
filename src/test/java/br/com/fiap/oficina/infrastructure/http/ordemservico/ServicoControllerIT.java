package br.com.fiap.oficina.infrastructure.http.ordemservico;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class ServicoControllerIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void limparBanco() {
        jdbc.execute("truncate table servico restart identity cascade");
    }

    private String servico(String nome, String valorPadrao) {
        return "{\"nome\":\"%s\",\"valorPadrao\":%s}".formatted(nome, valorPadrao);
    }

    private int cadastrar(String nome, String valorPadrao) throws Exception {
        String corpo = mvc.perform(post("/servicos").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(servico(nome, valorPadrao)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.id");
    }

    @Test
    void cadastraServicoRetorna201() throws Exception {
        mvc.perform(post("/servicos").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(servico("Troca de oleo", "120.50")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Troca de oleo"))
                .andExpect(jsonPath("$.valorPadrao").value(120.50));
        mvc.perform(get("/servicos").with(jwt()))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void valorPadraoZeroOuNegativoRetorna422SemPersistir() throws Exception {
        mvc.perform(post("/servicos").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(servico("Troca de oleo", "0")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("VALOR_PADRAO_INVALIDO"));
        mvc.perform(post("/servicos").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(servico("Troca de oleo", "-10")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("VALOR_PADRAO_INVALIDO"));
        mvc.perform(get("/servicos").with(jwt()))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void requestSemNomeOuValorRetorna400() throws Exception {
        mvc.perform(post("/servicos").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(servico("", "10")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/servicos").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Alinhamento\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nomeRepetidoEmOutraCaixaRetorna409() throws Exception {
        cadastrar("Troca de oleo", "120");

        mvc.perform(post("/servicos").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(servico("TROCA DE OLEO", "80")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("SERVICO_DUPLICADO"));
    }

    @Test
    void alteraNomeEValorPadrao() throws Exception {
        int id = cadastrar("Alinhamento", "10");

        mvc.perform(put("/servicos/" + id).with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(servico("Alinhamento 3D", "90.5")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Alinhamento 3D"))
                .andExpect(jsonPath("$.valorPadrao").value(90.5));
        mvc.perform(get("/servicos/" + id).with(jwt()))
                .andExpect(jsonPath("$.nome").value("Alinhamento 3D"))
                .andExpect(jsonPath("$.valorPadrao").value(90.5));
    }

    @Test
    void alterarComNomeDeOutroServicoRetorna409() throws Exception {
        cadastrar("Alinhamento", "10");
        int id = cadastrar("Balanceamento", "10");

        mvc.perform(put("/servicos/" + id).with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(servico("alinhamento", "10")))
                .andExpect(status().isConflict());
        mvc.perform(get("/servicos/" + id).with(jwt()))
                .andExpect(jsonPath("$.nome").value("Balanceamento"));
    }

    @Test
    void alterarComValorInvalidoRetorna422() throws Exception {
        int id = cadastrar("Alinhamento", "10");

        mvc.perform(put("/servicos/" + id).with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(servico("Alinhamento", "0")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void alterarInexistenteRetorna404() throws Exception {
        mvc.perform(put("/servicos/999").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(servico("Alinhamento", "10")))
                .andExpect(status().isNotFound());
    }

    @Test
    void listaPaginada() throws Exception {
        cadastrar("Alinhamento", "10");
        cadastrar("Balanceamento", "10");
        cadastrar("Troca de oleo", "10");

        mvc.perform(get("/servicos").param("pagina", "1").param("tamanho", "2").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens.length()").value(1))
                .andExpect(jsonPath("$.itens[0].nome").value("Troca de oleo"))
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.pagina").value(1))
                .andExpect(jsonPath("$.tamanho").value(2));
    }

    @Test
    void buscaPorIdInexistenteRetorna404() throws Exception {
        mvc.perform(get("/servicos/999").with(jwt())).andExpect(status().isNotFound());
    }

    @Test
    void removeServico() throws Exception {
        int id = cadastrar("Alinhamento", "10");

        mvc.perform(delete("/servicos/" + id).with(jwt())).andExpect(status().isNoContent());
        mvc.perform(get("/servicos/" + id).with(jwt())).andExpect(status().isNotFound());
    }

    @Test
    void removerInexistenteRetorna404() throws Exception {
        mvc.perform(delete("/servicos/999").with(jwt())).andExpect(status().isNotFound());
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(get("/servicos")).andExpect(status().isUnauthorized());
        mvc.perform(post("/servicos").contentType(MediaType.APPLICATION_JSON).content(servico("A", "10")))
                .andExpect(status().isUnauthorized());
    }
}
