package br.com.fiap.oficina.domain.ordemservico;

import java.math.BigDecimal;

public record ServicoDaOrdem(Long servicoId, String nome, BigDecimal valor) {
}
