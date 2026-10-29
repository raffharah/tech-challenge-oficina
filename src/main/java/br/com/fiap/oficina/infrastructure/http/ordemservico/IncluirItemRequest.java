package br.com.fiap.oficina.infrastructure.http.ordemservico;

import jakarta.validation.constraints.NotNull;

public record IncluirItemRequest(@NotNull Long itemId, @NotNull Integer quantidade) {
}
