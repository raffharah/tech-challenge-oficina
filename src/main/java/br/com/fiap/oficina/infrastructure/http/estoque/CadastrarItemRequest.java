package br.com.fiap.oficina.infrastructure.http.estoque;

import br.com.fiap.oficina.domain.estoque.TipoItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CadastrarItemRequest(
        @Schema(description = "Código único do item.", example = "PEC-PALHETA-21POL")
        @NotBlank String sku,
        @Schema(description = "Descrição do item.", example = "Palheta do limpador de para-brisa 21\"")
        @NotBlank String nome,
        @Schema(description = "PECA ou INSUMO.", example = "PECA")
        @NotNull TipoItem tipo,
        @Schema(description = "Preço de venda unitário, maior que zero.", example = "45.90")
        @NotNull BigDecimal precoVenda,
        @Schema(description = "Saldo físico inicial, zero ou mais.", example = "20")
        @NotNull Integer saldoInicial) {
}
