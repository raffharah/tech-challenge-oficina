package br.com.fiap.oficina.infrastructure.http.ordemservico;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record IncluirItemRequest(
        @Schema(description = "Id da peça ou insumo no estoque.", example = "1")
        @NotNull Long itemId,
        @Schema(description = "Quantidade, maior que zero; soma à já incluída do mesmo item.", example = "4")
        @NotNull Integer quantidade) {
}
