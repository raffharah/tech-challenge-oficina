package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.VeiculoIdentificado;
import java.time.Instant;

final class OrdemServicoEmDiagnostico {

    private OrdemServicoEmDiagnostico() {
    }

    static Long salvarEm(OrdemServicoRepositoryEmMemoria ordens) {
        OrdemServico ordem = OrdemServico.abrir(new VeiculoIdentificado(7L, "52998224725", "ABC1D23"),
                Instant.parse("2026-01-10T10:00:00Z"));
        ordem.iniciarDiagnostico(Instant.parse("2026-01-10T10:30:00Z"));
        return ordens.salvar(ordem).getId();
    }
}
