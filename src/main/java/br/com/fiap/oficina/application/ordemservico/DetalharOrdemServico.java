package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;

public class DetalharOrdemServico {

    private final OrdemServicoRepository ordens;

    public DetalharOrdemServico(OrdemServicoRepository ordens) {
        this.ordens = ordens;
    }

    public OrdemServicoDTO executar(Long ordemId) {
        return OrdemServicoDTO.de(ordens.obterPorId(ordemId));
    }
}
