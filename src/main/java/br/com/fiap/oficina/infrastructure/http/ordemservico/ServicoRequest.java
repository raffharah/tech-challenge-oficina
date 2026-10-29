package br.com.fiap.oficina.infrastructure.http.ordemservico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ServicoRequest(@NotBlank String nome, @NotNull BigDecimal valorPadrao) {
}
