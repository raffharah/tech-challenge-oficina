package br.com.fiap.oficina.infrastructure.http.estoque;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AtualizarItemRequest(@NotBlank String nome, @NotNull BigDecimal precoVenda) {
}
