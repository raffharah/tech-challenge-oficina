package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;
import java.time.Clock;

public class FinalizarOrdemServico {

    private final OrdemServicoRepository ordens;
    private final Clock clock;

    public FinalizarOrdemServico(OrdemServicoRepository ordens, Clock clock) {
        this.ordens = ordens;
        this.clock = clock;
    }

    public OrdemServicoDTO executar(Long ordemId) {
        OrdemServico ordem = ordens.obterPorId(ordemId);
        ordem.finalizar(clock.instant());
        return OrdemServicoDTO.de(ordens.salvar(ordem));
    }
}
