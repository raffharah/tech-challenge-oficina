package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;
import java.time.Clock;

public class RejeitarOrcamento {

    private final OrdemServicoRepository ordens;
    private final Clock clock;

    public RejeitarOrcamento(OrdemServicoRepository ordens, Clock clock) {
        this.ordens = ordens;
        this.clock = clock;
    }

    public OrdemServicoDTO executar(Long ordemId) {
        OrdemServico ordem = ordens.obterPorId(ordemId);
        ordem.rejeitar(clock.instant());
        return OrdemServicoDTO.de(ordens.salvar(ordem));
    }
}
