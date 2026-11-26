package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.NotificacaoDeStatus;
import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;
import java.time.Clock;

public class EntregarVeiculo {

    private final OrdemServicoRepository ordens;
    private final NotificacaoDeStatus notificacao;
    private final Clock clock;

    public EntregarVeiculo(OrdemServicoRepository ordens, NotificacaoDeStatus notificacao, Clock clock) {
        this.ordens = ordens;
        this.notificacao = notificacao;
        this.clock = clock;
    }

    public OrdemServicoDTO executar(Long ordemId) {
        OrdemServico ordem = ordens.obterPorId(ordemId);
        ordem.entregar(clock.instant());
        OrdemServico salva = ordens.salvar(ordem);
        notificacao.notificar(salva);
        return OrdemServicoDTO.de(salva);
    }
}
