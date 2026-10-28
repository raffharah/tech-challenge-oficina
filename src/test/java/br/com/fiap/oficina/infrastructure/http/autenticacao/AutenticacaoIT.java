package br.com.fiap.oficina.infrastructure.http.autenticacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.fiap.oficina.infrastructure.TestcontainersConfig;
import com.jayway.jsonpath.JsonPath;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfig.class, AutenticacaoIT.ControllerDeTeste.class})
class AutenticacaoIT {

    private static final String SENHA_DEMONSTRACAO = "oficina123";

    @Autowired
    MockMvc mvc;

    @Autowired
    JwtDecoder jwtDecoder;

    @Test
    void credencialValidaRetorna200ComTokenValidoPor60Minutos() throws Exception {
        String corpo = mvc.perform(post("/autenticacao").contentType(MediaType.APPLICATION_JSON)
                        .content(credenciais("funcionario", SENHA_DEMONSTRACAO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = JsonPath.read(corpo, "$.token");
        String expiraEm = JsonPath.read(corpo, "$.expiraEm");
        Jwt jwt = jwtDecoder.decode(token);

        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofMinutes(60));
        assertThat(Instant.parse(expiraEm)).isEqualTo(jwt.getExpiresAt());
        mvc.perform(get("/administrativo/teste").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void senhaInvalidaRetorna401SemToken() throws Exception {
        mvc.perform(post("/autenticacao").contentType(MediaType.APPLICATION_JSON)
                        .content(credenciais("funcionario", "senha-errada")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }

    @Test
    void usuarioInvalidoRetorna401SemToken() throws Exception {
        mvc.perform(post("/autenticacao").contentType(MediaType.APPLICATION_JSON)
                        .content(credenciais("desconhecido", SENHA_DEMONSTRACAO)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }

    private String credenciais(String usuario, String senha) {
        return "{\"usuario\":\"%s\",\"senha\":\"%s\"}".formatted(usuario, senha);
    }

    @RestController
    static class ControllerDeTeste {

        @GetMapping("/administrativo/teste")
        void administrativo() {
        }
    }
}
