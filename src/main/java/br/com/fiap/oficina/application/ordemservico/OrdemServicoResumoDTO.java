package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import java.math.BigDecimal;
import java.time.Instant;

public record OrdemServicoResumoDTO(Long id, String cpfCnpj, String placa, StatusOrdemServico status,
        BigDecimal total, Instant abertaEm) {

    static OrdemServicoResumoDTO de(OrdemServico ordem) {
        return new OrdemServicoResumoDTO(ordem.getId(), ordem.getCpfCnpj(), ordem.getPlaca(), ordem.getStatus(),
                ordem.getTotal(), ordem.getAbertaEm());
    }
}
