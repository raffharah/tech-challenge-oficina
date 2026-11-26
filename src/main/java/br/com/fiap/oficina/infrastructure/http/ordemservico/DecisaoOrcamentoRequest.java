package br.com.fiap.oficina.infrastructure.http.ordemservico;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DecisaoOrcamentoRequest(
        @Schema(description = "CPF ou CNPJ do titular da OS, com ou sem máscara.", example = "529.982.247-25")
        @NotBlank String cpfCnpj,
        @Schema(description = "`APROVADO` baixa os itens e leva a OS a `EM_EXECUCAO`; `RECUSADO` cancela a OS.",
                example = "APROVADO")
        @NotNull Decisao decisao) {

    public enum Decisao { APROVADO, RECUSADO }
}
