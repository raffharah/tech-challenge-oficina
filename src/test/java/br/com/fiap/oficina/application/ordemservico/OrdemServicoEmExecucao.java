package br.com.fiap.oficina.application.ordemservico;

import java.time.Instant;

final class OrdemServicoEmExecucao {

    private OrdemServicoEmExecucao() {
    }

    static Long salvarEm(OrdemServicoRepositoryEmMemoria ordens) {
        Long ordemId = OrdemServicoAguardandoAprovacao.salvarEm(ordens);
        var ordem = ordens.buscarPorId(ordemId).orElseThrow();
        ordem.aprovar(Instant.parse("2026-01-11T09:00:00Z"));
        ordens.salvar(ordem);
        return ordemId;
    }
}
