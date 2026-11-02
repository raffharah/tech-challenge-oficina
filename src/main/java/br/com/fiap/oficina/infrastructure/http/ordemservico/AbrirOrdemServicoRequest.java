package br.com.fiap.oficina.infrastructure.http.ordemservico;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AbrirOrdemServicoRequest(
        @Schema(description = "CPF ou CNPJ do cliente cadastrado, com ou sem máscara.", example = "529.982.247-25")
        @NotBlank String cpfCnpj,
        @Schema(description = "Placa de um veículo do cliente, com ou sem hífen.", example = "BRA2E19")
        @NotBlank String placa) {
}
