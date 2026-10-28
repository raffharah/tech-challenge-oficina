package br.com.fiap.oficina.domain.ordemservico;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import java.util.Optional;

public interface OrdemServicoRepository {

    OrdemServico salvar(OrdemServico ordemServico);

    Optional<OrdemServico> buscarPorId(Long id);

    default OrdemServico obterPorId(Long id) {
        return buscarPorId(id).orElseThrow(
                () -> new NaoEncontradoException("ORDEM_SERVICO_NAO_ENCONTRADA", "Ordem de servico nao encontrada"));
    }
}
