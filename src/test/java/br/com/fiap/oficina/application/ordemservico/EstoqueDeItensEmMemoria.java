package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.ordemservico.EstoqueDeItens;
import br.com.fiap.oficina.domain.ordemservico.ItemCotado;
import br.com.fiap.oficina.domain.ordemservico.ItemDaOrdem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class EstoqueDeItensEmMemoria implements EstoqueDeItens {

    private final Map<Long, ItemCotado> catalogo;
    private final boolean semSaldo;
    final List<List<ItemDaOrdem>> baixas = new ArrayList<>();

    EstoqueDeItensEmMemoria(Map<Long, ItemCotado> catalogo, boolean semSaldo) {
        this.catalogo = catalogo;
        this.semSaldo = semSaldo;
    }

    EstoqueDeItensEmMemoria(Map<Long, ItemCotado> catalogo) {
        this(catalogo, false);
    }

    @Override
    public ItemCotado cotar(Long itemId) {
        ItemCotado item = catalogo.get(itemId);
        if (item == null) {
            throw new DomainException("ITEM_NAO_ENCONTRADO", "Item nao encontrado");
        }
        return item;
    }

    @Override
    public void baixar(List<ItemDaOrdem> itens) {
        if (semSaldo) {
            throw new DomainException("SALDO_INSUFICIENTE", "Saldo fisico insuficiente para a baixa");
        }
        baixas.add(itens);
    }
}
