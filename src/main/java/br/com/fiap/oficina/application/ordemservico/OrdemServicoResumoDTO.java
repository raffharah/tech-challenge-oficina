package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import java.math.BigDecimal;

public record OrdemServicoResumoDTO(Long id, String cpfCnpj, String placa, StatusOrdemServico status,
        BigDecimal total) {

    static OrdemServicoResumoDTO de(OrdemServico ordem) {
        return new OrdemServicoResumoDTO(ordem.getId(), ordem.getCpfCnpj(), ordem.getPlaca(), ordem.getStatus(),
                ordem.getTotal());
    }
}
