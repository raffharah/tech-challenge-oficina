package br.com.fiap.oficina.application.estoque;

import br.com.fiap.oficina.domain.estoque.TipoItem;
import java.math.BigDecimal;

public record CadastrarItemDTO(String sku, String nome, TipoItem tipo, BigDecimal precoVenda, int saldoInicial) {
}
