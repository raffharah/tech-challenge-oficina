package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.Servico;
import java.math.BigDecimal;

public record ServicoDTO(Long id, String nome, BigDecimal valorPadrao) {

    static ServicoDTO de(Servico servico) {
        return new ServicoDTO(servico.getId(), servico.getNome(), servico.getValorPadrao());
    }
}
