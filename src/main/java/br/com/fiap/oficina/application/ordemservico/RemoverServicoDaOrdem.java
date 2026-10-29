package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;

public class RemoverServicoDaOrdem {

    private final OrdemServicoRepository ordens;

    public RemoverServicoDaOrdem(OrdemServicoRepository ordens) {
        this.ordens = ordens;
    }

    public void executar(Long ordemId, Long servicoId) {
        OrdemServico ordem = ordens.obterPorId(ordemId);
        ordem.removerServico(servicoId);
        ordens.salvar(ordem);
    }
}
