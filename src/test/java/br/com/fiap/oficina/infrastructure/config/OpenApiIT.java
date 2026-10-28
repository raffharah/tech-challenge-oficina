package br.com.fiap.oficina.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.fiap.oficina.infrastructure.TestcontainersConfig;
import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    void swaggerUiResponde200SemToken() throws Exception {
        assertThat(get("/swagger-ui.html").statusCode()).isEqualTo(200);
    }

    private HttpResponse<String> get(String caminho) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho)).build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
