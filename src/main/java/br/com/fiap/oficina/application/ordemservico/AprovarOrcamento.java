package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.CadastroDeVeiculos;
import br.com.fiap.oficina.domain.ordemservico.EstoqueDeItens;
import br.com.fiap.oficina.domain.ordemservico.NotificacaoDeStatus;
import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;
import java.time.Clock;

public class AprovarOrcamento {

    private final OrdemServicoRepository ordens;
    private final OrdensDoTitular ordensDoTitular;
    private final EstoqueDeItens estoque;
    private final NotificacaoDeStatus notificacao;
    private final Clock clock;

    public AprovarOrcamento(OrdemServicoRepository ordens, CadastroDeVeiculos veiculos, EstoqueDeItens estoque,
            NotificacaoDeStatus notificacao, Clock clock) {
        this.ordens = ordens;
        this.ordensDoTitular = new OrdensDoTitular(ordens, veiculos);
        this.estoque = estoque;
        this.notificacao = notificacao;
        this.clock = clock;
    }

    public OrdemServicoDTO executar(Long ordemId, String cpfCnpj) {
        OrdemServico ordem = ordensDoTitular.obter(ordemId, cpfCnpj);
        ordem.aprovar(clock.instant());
        estoque.baixar(ordem.getItens());
        OrdemServico salva = ordens.salvar(ordem);
        notificacao.notificar(salva);
        return OrdemServicoDTO.de(salva);
    }
}
