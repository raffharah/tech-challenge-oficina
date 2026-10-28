package br.com.fiap.oficina.application.estoque;

import java.math.BigDecimal;

public record AtualizarItemDTO(Long id, String nome, BigDecimal precoVenda) {
}
