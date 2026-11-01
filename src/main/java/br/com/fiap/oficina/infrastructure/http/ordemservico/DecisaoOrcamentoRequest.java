package br.com.fiap.oficina.infrastructure.http.ordemservico;

import jakarta.validation.constraints.NotBlank;

public record DecisaoOrcamentoRequest(@NotBlank String cpfCnpj) {
}
