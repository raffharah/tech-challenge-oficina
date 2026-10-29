package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.VeiculoIdentificado;
import java.time.Instant;

final class OrdemServicoEmDiagnostico {

    private OrdemServicoEmDiagnostico() {
    }

    static Long salvarEm(OrdemServicoRepositoryEmMemoria ordens) {
        return ordens.salvar(OrdemServico.abrir(new VeiculoIdentificado(7L, "52998224725", "ABC1D23"),
                Instant.parse("2026-01-10T10:00:00Z"))).getId();
    }
}
