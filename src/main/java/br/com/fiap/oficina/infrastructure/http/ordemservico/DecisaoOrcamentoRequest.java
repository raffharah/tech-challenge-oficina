package br.com.fiap.oficina.infrastructure.http.ordemservico;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record DecisaoOrcamentoRequest(
        @Schema(description = "CPF ou CNPJ do titular da OS, com ou sem máscara.", example = "529.982.247-25")
        @NotBlank String cpfCnpj) {
}
