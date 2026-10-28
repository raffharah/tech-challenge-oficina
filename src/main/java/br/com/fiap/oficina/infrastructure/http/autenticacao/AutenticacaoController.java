package br.com.fiap.oficina.infrastructure.http.autenticacao;

import jakarta.validation.Valid;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AutenticacaoController {

    private static final Duration VALIDADE = Duration.ofMinutes(60);

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final Clock clock;

    public AutenticacaoController(AuthenticationManager authenticationManager, JwtEncoder jwtEncoder, Clock clock) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.clock = clock;
    }

    @PostMapping("/autenticacao")
    ResponseEntity<AutenticacaoResponse> autenticar(@Valid @RequestBody AutenticacaoRequest request) {
        try {
            authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.usuario(), request.senha()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).build();
        }
        Instant emissao = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        Instant expiracao = emissao.plus(VALIDADE);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(request.usuario())
                .issuedAt(emissao)
                .expiresAt(expiracao)
                .build();
        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return ResponseEntity.ok(new AutenticacaoResponse(token, expiracao));
    }
}
