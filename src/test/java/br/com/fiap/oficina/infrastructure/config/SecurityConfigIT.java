package br.com.fiap.oficina.infrastructure.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.fiap.oficina.infrastructure.TestcontainersConfig;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfig.class, SecurityConfigIT.ControllerDeTeste.class})
class SecurityConfigIT {

    @Autowired
    MockMvc mvc;

    @Value("${oficina.jwt.segredo}")
    String segredo;

    @Test
    void rotaAdministrativaSemTokenRetorna401() throws Exception {
        mvc.perform(get("/administrativo/teste"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenComAssinaturaInvalidaRetorna401() throws Exception {
        String token = gerarToken("outro-segredo-diferente-com-32-bytes-ou-mais!", Instant.now().plus(1, ChronoUnit.HOURS));

        mvc.perform(get("/administrativo/teste").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenExpiradoRetorna401() throws Exception {
        String token = gerarToken(segredo, Instant.now().minus(2, ChronoUnit.HOURS));

        mvc.perform(get("/administrativo/teste").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenValidoAcessaRotaAdministrativa() throws Exception {
        String token = gerarToken(segredo, Instant.now().plus(1, ChronoUnit.HOURS));

        mvc.perform(get("/administrativo/teste").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void rotaDeClienteSemTokenEPublica() throws Exception {
        mvc.perform(get("/cliente/teste"))
                .andExpect(status().isOk());
    }

    @Test
    void documentacaoOpenApiSemTokenEPublica() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    private String gerarToken(String chave, Instant expiracao) {
        var key = new SecretKeySpec(chave.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        var encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        var claims = JwtClaimsSet.builder()
                .subject("funcionario")
                .issuedAt(expiracao.minus(1, ChronoUnit.HOURS))
                .expiresAt(expiracao)
                .build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    @RestController
    static class ControllerDeTeste {

        @GetMapping("/administrativo/teste")
        void administrativo() {
        }

        @GetMapping("/cliente/teste")
        void cliente() {
        }
    }
}
