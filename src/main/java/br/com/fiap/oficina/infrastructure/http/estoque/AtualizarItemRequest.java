package br.com.fiap.oficina.infrastructure.http.estoque;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AtualizarItemRequest(
        @Schema(description = "Descrição do item.", example = "Palheta do limpador de para-brisa 21\" silicone")
        @NotBlank String nome,
        @Schema(description = "Preço de venda unitário, maior que zero.", example = "49.90")
        @NotNull BigDecimal precoVenda) {
}
