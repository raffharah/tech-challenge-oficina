package br.com.fiap.oficina.infrastructure.http.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AtualizarVeiculoRequest(
        @Schema(description = "Marca do veículo.", example = "Volkswagen")
        @NotBlank String marca,
        @Schema(description = "Modelo do veículo.", example = "Gol 1.0 MPI")
        @NotBlank String modelo,
        @Schema(description = "Ano de fabricação, de 1900 até o ano seguinte ao atual.", example = "2019")
        @NotNull Integer ano) {
}
