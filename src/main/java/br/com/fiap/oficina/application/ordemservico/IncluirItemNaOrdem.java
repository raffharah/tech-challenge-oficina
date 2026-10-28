package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.EstoqueDeItens;
import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;

public class IncluirItemNaOrdem {

    private final OrdemServicoRepository ordens;
    private final EstoqueDeItens estoque;

    public IncluirItemNaOrdem(OrdemServicoRepository ordens, EstoqueDeItens estoque) {
        this.ordens = ordens;
        this.estoque = estoque;
    }

    public OrdemServicoDTO executar(Long ordemId, Long itemId, int quantidade) {
        OrdemServico ordem = ordens.obterPorId(ordemId);
        ordem.incluirItem(estoque.cotar(itemId), quantidade);
        return OrdemServicoDTO.de(ordens.salvar(ordem));
    }
}
