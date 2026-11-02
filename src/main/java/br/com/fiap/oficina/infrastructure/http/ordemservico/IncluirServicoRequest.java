package br.com.fiap.oficina.infrastructure.http.ordemservico;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record IncluirServicoRequest(
        @Schema(description = "Id do serviço no catálogo.", example = "1")
        @NotNull Long servicoId) {
}
