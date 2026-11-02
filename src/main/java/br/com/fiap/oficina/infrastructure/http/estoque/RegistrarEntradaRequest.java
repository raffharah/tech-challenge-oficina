package br.com.fiap.oficina.infrastructure.http.estoque;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record RegistrarEntradaRequest(
        @Schema(description = "Quantidade recebida, maior que zero.", example = "5")
        @NotNull Integer quantidade) {
}
