package br.com.fiap.oficina.application.ordemservico;

import java.math.BigDecimal;

public record AtualizarServicoDTO(Long id, String nome, BigDecimal valorPadrao) {
}
