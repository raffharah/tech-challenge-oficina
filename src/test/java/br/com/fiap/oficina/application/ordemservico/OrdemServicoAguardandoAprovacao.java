package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.ItemCotado;
import br.com.fiap.oficina.domain.ordemservico.Servico;

import java.math.BigDecimal;
import java.time.Instant;

final class OrdemServicoAguardandoAprovacao {

    private OrdemServicoAguardandoAprovacao() {
    }

    static Long salvarEm(OrdemServicoRepositoryEmMemoria ordens) {
        Long ordemId = OrdemServicoEmDiagnostico.salvarEm(ordens);
        var ordem = ordens.buscarPorId(ordemId).orElseThrow();
        ordem.incluirServico(new Servico(1L, "Troca de oleo", new BigDecimal("100.00")));
        ordem.incluirItem(new ItemCotado(10L, "OLE-1", "Oleo", new BigDecimal("35.50")), 2);
        ordem.concluirDiagnostico(Instant.parse("2026-01-10T11:00:00Z"));
        ordens.salvar(ordem);
        return ordemId;
    }
}
