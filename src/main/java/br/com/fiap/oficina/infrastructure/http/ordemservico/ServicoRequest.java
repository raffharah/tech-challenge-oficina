package br.com.fiap.oficina.infrastructure.http.ordemservico;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ServicoRequest(
        @Schema(description = "Nome único do serviço.", example = "Alinhamento e balanceamento")
        @NotBlank String nome,
        @Schema(description = "Valor da mão de obra, maior que zero.", example = "120.00")
        @NotNull BigDecimal valorPadrao) {
}
