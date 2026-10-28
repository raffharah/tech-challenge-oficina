package br.com.fiap.oficina.infrastructure.http.estoque;

import jakarta.validation.constraints.NotNull;

public record RegistrarEntradaRequest(@NotNull Integer quantidade) {
}
