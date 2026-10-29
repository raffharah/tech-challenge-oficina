package br.com.fiap.oficina.application.estoque;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.estoque.ItemEstoqueRepository;

public class RemoverItem {

    private final ItemEstoqueRepository itens;

    public RemoverItem(ItemEstoqueRepository itens) {
        this.itens = itens;
    }

    public void executar(Long id) {
        if (itens.buscarPorId(id).isEmpty()) {
            throw new NaoEncontradoException("ITEM_NAO_ENCONTRADO", "Item nao encontrado");
        }
        itens.remover(id);
    }
}
