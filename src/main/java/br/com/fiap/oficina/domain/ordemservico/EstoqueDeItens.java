package br.com.fiap.oficina.domain.ordemservico;

import java.util.List;

public interface EstoqueDeItens {

    ItemCotado cotar(Long itemId);

    void baixar(List<ItemDaOrdem> itens);
}
