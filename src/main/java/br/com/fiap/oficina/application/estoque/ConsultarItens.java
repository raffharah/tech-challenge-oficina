package br.com.fiap.oficina.application.estoque;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.Pagina;
import br.com.fiap.oficina.domain.estoque.ItemEstoqueRepository;

public class ConsultarItens {

    private final ItemEstoqueRepository itens;

    public ConsultarItens(ItemEstoqueRepository itens) {
        this.itens = itens;
    }

    public ItemEstoqueDTO buscarPorId(Long id) {
        return itens.buscarPorId(id).map(ItemEstoqueDTO::de)
                .orElseThrow(() -> new NaoEncontradoException("ITEM_NAO_ENCONTRADO", "Item nao encontrado"));
    }

    public Pagina<ItemEstoqueDTO> listar(int pagina, int tamanho) {
        return itens.listar(pagina, tamanho).map(ItemEstoqueDTO::de);
    }
}
