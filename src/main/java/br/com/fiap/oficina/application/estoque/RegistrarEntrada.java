package br.com.fiap.oficina.application.estoque;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.estoque.ItemEstoque;
import br.com.fiap.oficina.domain.estoque.ItemEstoqueRepository;

public class RegistrarEntrada {

    private final ItemEstoqueRepository itens;

    public RegistrarEntrada(ItemEstoqueRepository itens) {
        this.itens = itens;
    }

    public ItemEstoqueDTO executar(RegistrarEntradaDTO dto) {
        ItemEstoque item = itens.buscarPorId(dto.itemId())
                .orElseThrow(() -> new NaoEncontradoException("ITEM_NAO_ENCONTRADO", "Item nao encontrado"));
        item.registrarEntrada(dto.quantidade());
        return ItemEstoqueDTO.de(itens.salvar(item));
    }
}
