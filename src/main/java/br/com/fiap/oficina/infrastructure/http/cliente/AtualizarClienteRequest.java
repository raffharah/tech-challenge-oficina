package br.com.fiap.oficina.infrastructure.http.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AtualizarClienteRequest(
        @Schema(description = "Novo nome do cliente.", example = "Carlos Andrade Silva")
        @NotBlank String nome) {
}
