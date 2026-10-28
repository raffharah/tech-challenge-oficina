package br.com.fiap.oficina.infrastructure.http.autenticacao;

import jakarta.validation.constraints.NotBlank;

public record AutenticacaoRequest(@NotBlank String usuario, @NotBlank String senha) {
}
