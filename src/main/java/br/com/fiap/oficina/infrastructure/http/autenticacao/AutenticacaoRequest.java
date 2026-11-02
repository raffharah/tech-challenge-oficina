package br.com.fiap.oficina.infrastructure.http.autenticacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AutenticacaoRequest(
        @Schema(description = "Usuário do funcionário.", example = "funcionario")
        @NotBlank String usuario,
        @Schema(description = "Senha do funcionário.", example = "oficina123")
        @NotBlank String senha) {
}
