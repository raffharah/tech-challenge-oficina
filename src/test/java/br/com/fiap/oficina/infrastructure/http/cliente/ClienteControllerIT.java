package br.com.fiap.oficina.infrastructure.http.cliente;

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
class ClienteControllerIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void limparBanco() {
        jdbc.execute("truncate table veiculo, cliente restart identity");
    }

    private String cliente(String nome, String cpfCnpj) {
        return "{\"nome\":\"%s\",\"cpfCnpj\":\"%s\"}".formatted(nome, cpfCnpj);
    }

    private int cadastrar(String nome, String cpfCnpj) throws Exception {
        String corpo = mvc.perform(post("/clientes").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(cliente(nome, cpfCnpj)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.id");
    }

    @Test
    void cadastraCpfCnpjNumericoEAlfanumerico() throws Exception {
        mvc.perform(post("/clientes").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(cliente("Maria", "529.982.247-25")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Maria"))
                .andExpect(jsonPath("$.cpfCnpj").value("52998224725"));
        mvc.perform(post("/clientes").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(cliente("Oficina", "11.222.333/0001-81")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpfCnpj").value("11222333000181"));
        mvc.perform(post("/clientes").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(cliente("Alfa", "12.ABC.345/01DE-35")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpfCnpj").value("12ABC34501DE35"));
    }

    @Test
    void cpfCnpjInvalidoRetorna422SemPersistir() throws Exception {
        mvc.perform(post("/clientes").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(cliente("Maria", "111.111.111-11")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("CPF_CNPJ_INVALIDO"));
        mvc.perform(get("/clientes").with(jwt()))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void requestSemNomeRetorna400() throws Exception {
        mvc.perform(post("/clientes").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(cliente("", "52998224725")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cpfCnpjRepetidoRetorna409() throws Exception {
        cadastrar("Maria", "52998224725");

        mvc.perform(post("/clientes").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(cliente("Outra", "529.982.247-25")))
                .andExpect(status().isConflict());
    }

    @Test
    void atualizaNomeMantendoCpfCnpj() throws Exception {
        int id = cadastrar("Maria", "52998224725");

        mvc.perform(put("/clientes/" + id).with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Maria Silva\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Silva"))
                .andExpect(jsonPath("$.cpfCnpj").value("52998224725"));
        mvc.perform(get("/clientes/" + id).with(jwt()))
                .andExpect(jsonPath("$.nome").value("Maria Silva"));
    }

    @Test
    void atualizarInexistenteRetorna404() throws Exception {
        mvc.perform(put("/clientes/999").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"X\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listaPaginada() throws Exception {
        cadastrar("Maria", "52998224725");
        cadastrar("Oficina", "11222333000181");
        cadastrar("Alfa", "12ABC34501DE35");

        mvc.perform(get("/clientes").param("pagina", "1").param("tamanho", "2").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens.length()").value(1))
                .andExpect(jsonPath("$.itens[0].nome").value("Alfa"))
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.pagina").value(1))
                .andExpect(jsonPath("$.tamanho").value(2));
    }

    @Test
    void buscaPorCpfCnpjComMascaraEMinusculas() throws Exception {
        cadastrar("Maria", "52998224725");
        cadastrar("Alfa", "12ABC34501DE35");

        mvc.perform(get("/clientes").param("cpfCnpj", "529.982.247-25").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria"));
        mvc.perform(get("/clientes").param("cpfCnpj", "12.abc.345/01de-35").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Alfa"));
    }

    @Test
    void buscaPorCpfCnpjNaoCadastradoRetorna404EInvalidoRetorna422() throws Exception {
        mvc.perform(get("/clientes").param("cpfCnpj", "52998224725").with(jwt()))
                .andExpect(status().isNotFound());
        mvc.perform(get("/clientes").param("cpfCnpj", "123").with(jwt()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void buscaPorIdInexistenteRetorna404() throws Exception {
        mvc.perform(get("/clientes/999").with(jwt())).andExpect(status().isNotFound());
    }

    @Test
    void removeClienteSemVeiculos() throws Exception {
        int id = cadastrar("Maria", "52998224725");

        mvc.perform(delete("/clientes/" + id).with(jwt())).andExpect(status().isNoContent());
        mvc.perform(get("/clientes/" + id).with(jwt())).andExpect(status().isNotFound());
    }

    @Test
    void removerClienteComVeiculoRetorna409() throws Exception {
        int id = cadastrar("Maria", "52998224725");
        mvc.perform(post("/veiculos").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"ABC1234\",\"marca\":\"Fiat\",\"modelo\":\"Uno\",\"ano\":2010,"
                                + "\"clienteId\":" + id + "}"))
                .andExpect(status().isCreated());

        mvc.perform(delete("/clientes/" + id).with(jwt())).andExpect(status().isConflict());
        mvc.perform(get("/clientes/" + id).with(jwt())).andExpect(status().isOk());
    }

    @Test
    void removerInexistenteRetorna404() throws Exception {
        mvc.perform(delete("/clientes/999").with(jwt())).andExpect(status().isNotFound());
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(get("/clientes")).andExpect(status().isUnauthorized());
        mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(cliente("M", "52998224725")))
                .andExpect(status().isUnauthorized());
    }
}
