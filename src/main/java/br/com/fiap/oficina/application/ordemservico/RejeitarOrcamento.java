package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.CadastroDeVeiculos;
import br.com.fiap.oficina.domain.ordemservico.NotificacaoDeStatus;
import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;
import java.time.Clock;

public class RejeitarOrcamento {

    private final OrdemServicoRepository ordens;
    private final OrdensDoTitular ordensDoTitular;
    private final NotificacaoDeStatus notificacao;
    private final Clock clock;

    public RejeitarOrcamento(OrdemServicoRepository ordens, CadastroDeVeiculos veiculos,
            NotificacaoDeStatus notificacao, Clock clock) {
        this.ordens = ordens;
        this.ordensDoTitular = new OrdensDoTitular(ordens, veiculos);
        this.notificacao = notificacao;
        this.clock = clock;
    }

    public OrdemServicoDTO executar(Long ordemId, String cpfCnpj) {
        OrdemServico ordem = ordensDoTitular.obter(ordemId, cpfCnpj);
        ordem.rejeitar(clock.instant());
        OrdemServico salva = ordens.salvar(ordem);
        notificacao.notificar(salva);
        return OrdemServicoDTO.de(salva);
    }
}
