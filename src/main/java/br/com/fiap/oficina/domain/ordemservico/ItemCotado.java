package br.com.fiap.oficina.domain.ordemservico;

import java.math.BigDecimal;

public record ItemCotado(Long itemId, String sku, String nome, BigDecimal precoVenda) {
}
