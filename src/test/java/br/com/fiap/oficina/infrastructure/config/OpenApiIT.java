package br.com.fiap.oficina.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.fiap.oficina.infrastructure.TestcontainersConfig;
import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfig.class)
class OpenApiIT {

    @LocalServerPort
    int porta;

    private final HttpClient http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

    @Test
    void apiDocsDeclaraEsquemaBearerSemToken() throws Exception {
        HttpResponse<String> resposta = get("/v3/api-docs");

        assertThat(resposta.statusCode()).isEqualTo(200);
        Map<String, Object> bearer = JsonPath.read(resposta.body(), "$.components.securitySchemes.bearer");
        assertThat(bearer).containsEntry("type", "http").containsEntry("scheme", "bearer")
                .containsEntry("bearerFormat", "JWT");
    }

    @Test
    void todaOperacaoTemResumoDescricaoETag() throws Exception {
        String docs = get("/v3/api-docs").body();

        List<Map<String, Object>> operacoes = JsonPath.read(docs, "$.paths.*.*");
        assertThat(operacoes).isNotEmpty().allSatisfy(operacao -> {
            assertThat(operacao.get("summary")).as("summary de %s", operacao.get("operationId")).isNotNull();
            assertThat(operacao.get("description")).as("description de %s", operacao.get("operationId"))
                    .isNotNull();
            assertThat(operacao.get("tags")).as("tags de %s", operacao.get("operationId")).isNotNull();
        });
        List<Map<String, Object>> tags = JsonPath.read(docs, "$.tags");
        assertThat(tags).isNotEmpty().allSatisfy(tag -> assertThat(tag.get("description")).isNotNull());
    }

    @Test
    void todoCampoDeRequestTemDescricaoEExemplo() throws Exception {
        String docs = get("/v3/api-docs").body();

        Map<String, Map<String, Object>> schemas = JsonPath.read(docs, "$.components.schemas");
        List<String> requests = schemas.keySet().stream().filter(nome -> nome.endsWith("Request")).toList();
        assertThat(requests).isNotEmpty();
        for (String request : requests) {
            Map<String, Map<String, Object>> campos = JsonPath.read(docs,
                    "$.components.schemas." + request + ".properties");
            campos.forEach((campo, schema) -> {
                assertThat(schema.get("description")).as("%s.%s description", request, campo).isNotNull();
                assertThat(schema.get("example")).as("%s.%s example", request, campo).isNotNull();
            });
        }
    }

    @Test
    void parametrosDeConsultaTemDescricaoEExemplo() throws Exception {
        String docs = get("/v3/api-docs").body();

        List<Map<String, Object>> parametros = JsonPath.read(docs, "$.paths.*.*.parameters[*]");
        assertThat(parametros).isNotEmpty().allSatisfy(parametro -> {
            assertThat(parametro.get("description")).as("description de %s", parametro.get("name")).isNotNull();
            assertThat(parametro.get("example")).as("example de %s", parametro.get("name")).isNotNull();
        });
    }

    @Test
    void listagensDeClientesEVeiculosNaoExigemFiltro() throws Exception {
        String docs = get("/v3/api-docs").body();

        for (String caminho : List.of("/clientes", "/veiculos")) {
            List<Boolean> obrigatorios = JsonPath.read(docs,
                    "$.paths['" + caminho + "'].get.parameters[*].required");
            assertThat(obrigatorios).as("parametros obrigatorios em GET %s", caminho).doesNotContain(true);
        }
    }

    @Test
    void errosDeNegocioDocumentadosComCorpoDeErro() throws Exception {
        String docs = get("/v3/api-docs").body();

        Map<String, Object> erro = JsonPath.read(docs,
                "$.paths['/ordens-servico/{id}/orcamento'].post.responses.422");
        assertThat((String) erro.get("description")).contains("ORDEM_SEM_SERVICO");
        String esquema = JsonPath.read(docs,
                "$.paths['/ordens-servico/{id}/orcamento'].post.responses.422.content['application/json'].schema.$ref");
        assertThat(esquema).endsWith("/ErroResponse");
        assertThat((Map<String, Object>) JsonPath.read(docs,
                "$.paths['/ordens-servico/{id}/orcamento'].post.responses")).containsKey("401");
        assertThat((Map<String, Object>) JsonPath.read(docs,
                "$.paths['/cliente/ordens-servico'].get.responses")).doesNotContainKey("401");
    }

    @Test
    void swaggerUiResponde200SemToken() throws Exception {
        assertThat(get("/swagger-ui.html").statusCode()).isEqualTo(200);
    }

    private HttpResponse<String> get(String caminho) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho)).build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
