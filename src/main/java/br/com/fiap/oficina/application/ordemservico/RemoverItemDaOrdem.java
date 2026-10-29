package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;

public class RemoverItemDaOrdem {

    private final OrdemServicoRepository ordens;

    public RemoverItemDaOrdem(OrdemServicoRepository ordens) {
        this.ordens = ordens;
    }

    public void executar(Long ordemId, Long itemId) {
        OrdemServico ordem = ordens.obterPorId(ordemId);
        ordem.removerItem(itemId);
        ordens.salvar(ordem);
    }
}
