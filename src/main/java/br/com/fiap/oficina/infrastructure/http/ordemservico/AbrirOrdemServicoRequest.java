package br.com.fiap.oficina.infrastructure.http.ordemservico;

import jakarta.validation.constraints.NotBlank;

public record AbrirOrdemServicoRequest(@NotBlank String cpfCnpj, @NotBlank String placa) {
}
