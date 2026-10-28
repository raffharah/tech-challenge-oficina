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
import java.time.Year;
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
class VeiculoControllerIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    private int clienteId;

    @BeforeEach
    void prepararBanco() throws Exception {
        jdbc.execute("truncate table veiculo, cliente restart identity cascade");
        clienteId = cadastrarCliente("Maria", "52998224725");
    }

    private int cadastrarCliente(String nome, String cpfCnpj) throws Exception {
        String corpo = mvc.perform(post("/clientes").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"%s\",\"cpfCnpj\":\"%s\"}".formatted(nome, cpfCnpj)))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.id");
    }

    private String veiculo(String placa, int ano, int dono) {
        return "{\"placa\":\"%s\",\"marca\":\"Fiat\",\"modelo\":\"Uno\",\"ano\":%d,\"clienteId\":%d}"
                .formatted(placa, ano, dono);
    }

    private int cadastrar(String placa, int ano, int dono) throws Exception {
        String corpo = mvc.perform(post("/veiculos").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(veiculo(placa, ano, dono)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.id");
    }

    @Test
    void cadastraComPlacaNormalizadaEBuscaPorPlaca() throws Exception {
        mvc.perform(post("/veiculos").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(veiculo("abc-1234", 2010, clienteId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.placa").value("ABC1234"))
                .andExpect(jsonPath("$.clienteId").value(clienteId));

        mvc.perform(get("/veiculos").param("placa", "abc-1234").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placa").value("ABC1234"));
        mvc.perform(get("/veiculos").param("placa", "ABC1234").with(jwt()))
                .andExpect(status().isOk());
    }

    @Test
    void placaMercosulAceita() throws Exception {
        mvc.perform(post("/veiculos").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(veiculo("ABC1D23", 2020, clienteId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.placa").value("ABC1D23"));
    }

    @Test
    void placaInvalidaRetorna422() throws Exception {
        mvc.perform(post("/veiculos").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(veiculo("AB12345", 2010, clienteId)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("PLACA_INVALIDA"));
    }

    @Test
    void placaRepetidaRetorna409() throws Exception {
        cadastrar("ABC1234", 2010, clienteId);

        mvc.perform(post("/veiculos").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(veiculo("abc-1234", 2011, clienteId)))
                .andExpect(status().isConflict());
    }

    @Test
    void anoForaDoLimiteRetorna422() throws Exception {
        int anoCorrente = Year.now().getValue();

        mvc.perform(post("/veiculos").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(veiculo("ABC1234", 1899, clienteId)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("ANO_VEICULO_INVALIDO"));
        mvc.perform(post("/veiculos").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(veiculo("ABC1234", anoCorrente + 2, clienteId)))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(post("/veiculos").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(veiculo("ABC1234", anoCorrente + 1, clienteId)))
                .andExpect(status().isCreated());
    }

    @Test
    void clienteInexistenteRetorna404() throws Exception {
        mvc.perform(post("/veiculos").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(veiculo("ABC1234", 2010, 999)))
                .andExpect(status().isNotFound());
    }

    @Test
    void requestSemMarcaRetorna400() throws Exception {
        mvc.perform(post("/veiculos").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"ABC1234\",\"modelo\":\"Uno\",\"ano\":2010,\"clienteId\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizaMantendoPlacaECliente() throws Exception {
        int id = cadastrar("ABC1234", 2010, clienteId);

        mvc.perform(put("/veiculos/" + id).with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"marca\":\"VW\",\"modelo\":\"Gol\",\"ano\":2015}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marca").value("VW"))
                .andExpect(jsonPath("$.modelo").value("Gol"))
                .andExpect(jsonPath("$.ano").value(2015))
                .andExpect(jsonPath("$.placa").value("ABC1234"))
                .andExpect(jsonPath("$.clienteId").value(clienteId));
    }

    @Test
    void atualizarInexistenteRetorna404() throws Exception {
        mvc.perform(put("/veiculos/999").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"marca\":\"VW\",\"modelo\":\"Gol\",\"ano\":2015}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listaSoVeiculosDoCliente() throws Exception {
        int outro = cadastrarCliente("Oficina", "11222333000181");
        cadastrar("ABC1234", 2010, clienteId);
        cadastrar("DEF5678", 2012, outro);
        cadastrar("GHI1J23", 2020, clienteId);

        mvc.perform(get("/clientes/" + clienteId + "/veiculos").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].placa").value("ABC1234"))
                .andExpect(jsonPath("$[1].placa").value("GHI1J23"));
    }

    @Test
    void buscaInexistenteRetorna404() throws Exception {
        mvc.perform(get("/veiculos").param("placa", "ZZZ9999").with(jwt())).andExpect(status().isNotFound());
        mvc.perform(get("/veiculos/999").with(jwt())).andExpect(status().isNotFound());
    }

    @Test
    void removeVeiculo() throws Exception {
        int id = cadastrar("ABC1234", 2010, clienteId);

        mvc.perform(delete("/veiculos/" + id).with(jwt())).andExpect(status().isNoContent());
        mvc.perform(get("/veiculos/" + id).with(jwt())).andExpect(status().isNotFound());
        mvc.perform(delete("/veiculos/" + id).with(jwt())).andExpect(status().isNotFound());
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(get("/veiculos").param("placa", "ABC1234")).andExpect(status().isUnauthorized());
        mvc.perform(post("/veiculos").contentType(MediaType.APPLICATION_JSON).content(veiculo("ABC1234", 2010, 1)))
                .andExpect(status().isUnauthorized());
    }
}
