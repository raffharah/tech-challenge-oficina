package br.com.fiap.oficina.domain.ordemservico;

import java.math.BigDecimal;

public record ItemDaOrdem(Long itemId, String sku, String nome, int quantidade, BigDecimal precoUnitario) {
}
