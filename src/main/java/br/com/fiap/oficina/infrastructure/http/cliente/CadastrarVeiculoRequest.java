package br.com.fiap.oficina.infrastructure.http.cliente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CadastrarVeiculoRequest(@NotBlank String placa, @NotBlank String marca, @NotBlank String modelo,
        @NotNull Integer ano, @NotNull Long clienteId) {
}
