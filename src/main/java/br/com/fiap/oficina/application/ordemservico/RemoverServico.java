package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.ordemservico.ServicoRepository;

public class RemoverServico {

    private final ServicoRepository servicos;

    public RemoverServico(ServicoRepository servicos) {
        this.servicos = servicos;
    }

    public void executar(Long id) {
        if (servicos.buscarPorId(id).isEmpty()) {
            throw new NaoEncontradoException("SERVICO_NAO_ENCONTRADO", "Servico nao encontrado");
        }
        servicos.remover(id);
    }
}
