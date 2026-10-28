package br.com.fiap.oficina.application.ordemservico;

import java.math.BigDecimal;

public record CadastrarServicoDTO(String nome, BigDecimal valorPadrao) {
}
