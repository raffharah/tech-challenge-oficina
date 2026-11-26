package br.com.fiap.oficina.infrastructure.http.ordemservico;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AbrirOrdemServicoRequest(
        @Schema(description = "CPF ou CNPJ do cliente cadastrado, com ou sem máscara.", example = "529.982.247-25")
        @NotBlank String cpfCnpj,
        @Schema(description = "E-mail do cliente; grava ou substitui o e-mail cadastrado.",
                example = "maria@oficina.com")
        @NotBlank String email,
        @Schema(description = "Placa de um veículo do cliente, com ou sem hífen.", example = "BRA2E19")
        @NotBlank String placa,
        @Schema(description = "Ids dos serviços solicitados; ao menos um.", example = "[1]")
        @NotEmpty List<@NotNull Long> servicos,
        @Schema(description = "Peças e insumos solicitados; ao menos um. Item repetido soma a quantidade.",
                example = "[{\"itemId\": 1, \"quantidade\": 2}]")
        @NotEmpty List<@NotNull @Valid ItemSolicitadoRequest> itens) {

    public record ItemSolicitadoRequest(
            @Schema(description = "Id do item de estoque.", example = "1") @NotNull Long itemId,
            @Schema(description = "Quantidade solicitada, maior que zero.", example = "2") @NotNull Integer quantidade) {
    }
}
