package br.com.fiap.oficina.infrastructure.http.ordemservico;

import jakarta.validation.constraints.NotNull;

public record IncluirServicoRequest(@NotNull Long servicoId) {
}
