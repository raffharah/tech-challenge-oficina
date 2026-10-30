package br.com.fiap.oficina.domain.ordemservico;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.Pagina;
import java.util.List;
import java.util.Optional;

public interface OrdemServicoRepository {

    OrdemServico salvar(OrdemServico ordemServico);

    Optional<OrdemServico> buscarPorId(Long id);

    Pagina<OrdemServico> listar(int pagina, int tamanho);

    List<OrdemServico> listarFinalizadas();

    default OrdemServico obterPorId(Long id) {
        return buscarPorId(id).orElseThrow(
                () -> new NaoEncontradoException("ORDEM_SERVICO_NAO_ENCONTRADA", "Ordem de servico nao encontrada"));
    }
}
