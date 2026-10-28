package br.com.fiap.oficina.infrastructure.http.autenticacao;

import java.time.Instant;

public record AutenticacaoResponse(String token, Instant expiraEm) {
}
