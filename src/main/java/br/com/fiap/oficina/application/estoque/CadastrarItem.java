package br.com.fiap.oficina.application.estoque;

import br.com.fiap.oficina.domain.ConflitoException;
import br.com.fiap.oficina.domain.estoque.ItemEstoque;
import br.com.fiap.oficina.domain.estoque.ItemEstoqueRepository;

public class CadastrarItem {

    private final ItemEstoqueRepository itens;

    public CadastrarItem(ItemEstoqueRepository itens) {
        this.itens = itens;
    }

    public ItemEstoqueDTO executar(CadastrarItemDTO dto) {
        ItemEstoque item = new ItemEstoque(null, dto.sku(), dto.nome(), dto.tipo(), dto.precoVenda(),
                dto.saldoInicial());
        if (itens.existePorSku(item.getSku())) {
            throw new ConflitoException("SKU_DUPLICADO", "Ja existe item com este SKU");
        }
        return ItemEstoqueDTO.de(itens.salvar(item));
    }
}
