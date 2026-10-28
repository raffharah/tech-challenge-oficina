package br.com.fiap.oficina.infrastructure.http.cliente;

import jakarta.validation.constraints.NotBlank;

public record CadastrarClienteRequest(@NotBlank String nome, @NotBlank String cpfCnpj) {
}
