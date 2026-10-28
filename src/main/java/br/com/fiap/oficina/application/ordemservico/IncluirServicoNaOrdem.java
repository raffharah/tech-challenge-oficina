package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;
import br.com.fiap.oficina.domain.ordemservico.Servico;
import br.com.fiap.oficina.domain.ordemservico.ServicoRepository;

public class IncluirServicoNaOrdem {

    private final OrdemServicoRepository ordens;
    private final ServicoRepository servicos;

    public IncluirServicoNaOrdem(OrdemServicoRepository ordens, ServicoRepository servicos) {
        this.ordens = ordens;
        this.servicos = servicos;
    }

    public OrdemServicoDTO executar(Long ordemId, Long servicoId) {
        OrdemServico ordem = ordens.obterPorId(ordemId);
        Servico servico = servicos.buscarPorId(servicoId)
                .orElseThrow(() -> new DomainException("SERVICO_NAO_ENCONTRADO", "Servico nao encontrado"));
        ordem.incluirServico(servico);
        return OrdemServicoDTO.de(ordens.salvar(ordem));
    }
}
