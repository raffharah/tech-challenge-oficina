package br.com.fiap.oficina.application.estoque;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.estoque.ItemEstoque;
import br.com.fiap.oficina.domain.estoque.ItemEstoqueRepository;

public class AtualizarItem {

    private final ItemEstoqueRepository itens;

    public AtualizarItem(ItemEstoqueRepository itens) {
        this.itens = itens;
    }

    public ItemEstoqueDTO executar(AtualizarItemDTO dto) {
        ItemEstoque item = itens.buscarPorId(dto.id())
                .orElseThrow(() -> new NaoEncontradoException("ITEM_NAO_ENCONTRADO", "Item nao encontrado"));
        item.atualizar(dto.nome(), dto.precoVenda());
        return ItemEstoqueDTO.de(itens.salvar(item));
    }
}
