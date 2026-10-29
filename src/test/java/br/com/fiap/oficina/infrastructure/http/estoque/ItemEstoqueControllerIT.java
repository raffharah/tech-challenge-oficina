package br.com.fiap.oficina.infrastructure.http.estoque;

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
class ItemEstoqueControllerIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void limparBanco() {
        jdbc.execute("truncate table item_estoque restart identity cascade");
    }

    private String item(String sku, String nome, String tipo, String preco, int saldo) {
        return "{\"sku\":\"%s\",\"nome\":\"%s\",\"tipo\":\"%s\",\"precoVenda\":%s,\"saldoInicial\":%d}"
                .formatted(sku, nome, tipo, preco, saldo);
    }

    private int cadastrar(String sku, String tipo, String preco, int saldo) throws Exception {
        String corpo = mvc.perform(post("/itens").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(item(sku, "Item " + sku, tipo, preco, saldo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.id");
    }

    private String entrada(int quantidade) {
        return "{\"quantidade\":%d}".formatted(quantidade);
    }

    @Test
    void cadastraItemRetorna201ComSaldoFisicoIgualAoInicial() throws Exception {
        mvc.perform(post("/itens").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(item("FILTRO-01", "Filtro de oleo", "PECA", "45.50", 8)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.sku").value("FILTRO-01"))
                .andExpect(jsonPath("$.tipo").value("PECA"))
                .andExpect(jsonPath("$.precoVenda").value(45.50))
                .andExpect(jsonPath("$.saldoFisico").value(8));
    }

    @Test
    void skuRepetidoRetorna409() throws Exception {
        cadastrar("FILTRO-01", "PECA", "10", 1);

        mvc.perform(post("/itens").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(item("filtro-01", "Outro", "INSUMO", "5", 1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("SKU_DUPLICADO"));
    }

    @Test
    void precoNaoPositivoOuSaldoInicialNegativoRetorna422SemPersistir() throws Exception {
        mvc.perform(post("/itens").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(item("A", "Item", "PECA", "0", 1)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("PRECO_VENDA_INVALIDO"));
        mvc.perform(post("/itens").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(item("B", "Item", "PECA", "10", -1)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("SALDO_INICIAL_INVALIDO"));
        mvc.perform(get("/itens").with(jwt())).andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void requestSemCamposObrigatoriosRetorna400() throws Exception {
        mvc.perform(post("/itens").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"A\",\"nome\":\"Item\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/itens").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"A\",\"nome\":\"Item\",\"tipo\":\"PECA\",\"precoVenda\":10}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void alteraNomeEPrecoSemMudarSaldo() throws Exception {
        int id = cadastrar("A", "INSUMO", "10", 7);

        mvc.perform(put("/itens/" + id).with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Novo nome\",\"precoVenda\":22.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Novo nome"))
                .andExpect(jsonPath("$.precoVenda").value(22.5))
                .andExpect(jsonPath("$.saldoFisico").value(7));
        mvc.perform(get("/itens/" + id).with(jwt()))
                .andExpect(jsonPath("$.nome").value("Novo nome"))
                .andExpect(jsonPath("$.saldoFisico").value(7));
    }

    @Test
    void alterarComPrecoInvalidoRetorna422EInexistenteRetorna404() throws Exception {
        int id = cadastrar("A", "PECA", "10", 1);

        mvc.perform(put("/itens/" + id).with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Item\",\"precoVenda\":0}"))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(put("/itens/999").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Item\",\"precoVenda\":10}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listaPaginadaComTipoPrecoESaldo() throws Exception {
        cadastrar("A", "PECA", "10", 1);
        cadastrar("B", "INSUMO", "20", 2);
        cadastrar("C", "PECA", "30", 3);

        mvc.perform(get("/itens").param("pagina", "1").param("tamanho", "2").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens.length()").value(1))
                .andExpect(jsonPath("$.itens[0].sku").value("C"))
                .andExpect(jsonPath("$.itens[0].tipo").value("PECA"))
                .andExpect(jsonPath("$.itens[0].precoVenda").value(30))
                .andExpect(jsonPath("$.itens[0].saldoFisico").value(3))
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.pagina").value(1))
                .andExpect(jsonPath("$.tamanho").value(2));
    }

    @Test
    void buscaPorIdRetornaItemEInexistenteRetorna404() throws Exception {
        int id = cadastrar("A", "INSUMO", "12.5", 4);

        mvc.perform(get("/itens/" + id).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("INSUMO"))
                .andExpect(jsonPath("$.precoVenda").value(12.5))
                .andExpect(jsonPath("$.saldoFisico").value(4));
        mvc.perform(get("/itens/999").with(jwt())).andExpect(status().isNotFound());
    }

    @Test
    void entradaSomaAoSaldo() throws Exception {
        int id = cadastrar("A", "PECA", "10", 10);

        mvc.perform(post("/itens/" + id + "/entradas").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(entrada(5)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoFisico").value(15));
        mvc.perform(get("/itens/" + id).with(jwt())).andExpect(jsonPath("$.saldoFisico").value(15));
    }

    @Test
    void entradaZeroOuNegativaRetorna422SemAlterarSaldo() throws Exception {
        int id = cadastrar("A", "PECA", "10", 10);

        mvc.perform(post("/itens/" + id + "/entradas").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(entrada(0)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("QUANTIDADE_INVALIDA"));
        mvc.perform(post("/itens/" + id + "/entradas").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(entrada(-2)))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/itens/" + id).with(jwt())).andExpect(jsonPath("$.saldoFisico").value(10));
    }

    @Test
    void entradaEmItemInexistenteRetorna404() throws Exception {
        mvc.perform(post("/itens/999/entradas").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(entrada(1)))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeItem() throws Exception {
        int id = cadastrar("A", "PECA", "10", 1);

        mvc.perform(delete("/itens/" + id).with(jwt())).andExpect(status().isNoContent());
        mvc.perform(get("/itens/" + id).with(jwt())).andExpect(status().isNotFound());
    }

    @Test
    void removerInexistenteRetorna404() throws Exception {
        mvc.perform(delete("/itens/999").with(jwt())).andExpect(status().isNotFound());
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(get("/itens")).andExpect(status().isUnauthorized());
        mvc.perform(post("/itens").contentType(MediaType.APPLICATION_JSON).content(item("A", "I", "PECA", "10", 1)))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/itens/1/entradas").contentType(MediaType.APPLICATION_JSON).content(entrada(1)))
                .andExpect(status().isUnauthorized());
    }
}
