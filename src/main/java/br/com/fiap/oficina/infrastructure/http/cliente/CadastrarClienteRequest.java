package br.com.fiap.oficina.infrastructure.http.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CadastrarClienteRequest(
        @Schema(description = "Nome do cliente.", example = "Carlos Andrade")
        @NotBlank String nome,
        @Schema(description = "CPF ou CNPJ (numérico ou alfanumérico), com ou sem máscara.", example = "529.982.247-25")
        @NotBlank String cpfCnpj) {
}
