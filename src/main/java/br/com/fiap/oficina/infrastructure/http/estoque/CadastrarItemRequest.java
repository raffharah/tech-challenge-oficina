package br.com.fiap.oficina.infrastructure.http.estoque;

import br.com.fiap.oficina.domain.estoque.TipoItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CadastrarItemRequest(@NotBlank String sku, @NotBlank String nome, @NotNull TipoItem tipo,
        @NotNull BigDecimal precoVenda, @NotNull Integer saldoInicial) {
}
