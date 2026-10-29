package br.com.fiap.oficina.application.estoque;

import br.com.fiap.oficina.domain.estoque.ItemEstoque;
import br.com.fiap.oficina.domain.estoque.TipoItem;
import java.math.BigDecimal;

public record ItemEstoqueDTO(Long id, String sku, String nome, TipoItem tipo, BigDecimal precoVenda, int saldoFisico) {

    static ItemEstoqueDTO de(ItemEstoque item) {
        return new ItemEstoqueDTO(item.getId(), item.getSku(), item.getNome(), item.getTipo(), item.getPrecoVenda(),
                item.getSaldoFisico());
    }
}
