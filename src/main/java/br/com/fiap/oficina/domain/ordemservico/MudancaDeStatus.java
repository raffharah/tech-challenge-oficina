package br.com.fiap.oficina.domain.ordemservico;

import java.time.Instant;

public record MudancaDeStatus(StatusOrdemServico status, Instant dataHora) {
}
