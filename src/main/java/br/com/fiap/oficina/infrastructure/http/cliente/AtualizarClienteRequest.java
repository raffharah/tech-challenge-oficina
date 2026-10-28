package br.com.fiap.oficina.infrastructure.http.cliente;

import jakarta.validation.constraints.NotBlank;

public record AtualizarClienteRequest(@NotBlank String nome) {
}
