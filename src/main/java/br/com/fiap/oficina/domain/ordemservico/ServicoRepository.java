package br.com.fiap.oficina.domain.ordemservico;

import br.com.fiap.oficina.domain.Pagina;
import java.util.Optional;

public interface ServicoRepository {

    Servico salvar(Servico servico);

    Optional<Servico> buscarPorId(Long id);

    Optional<Servico> buscarPorNome(String nome);

    Pagina<Servico> listar(int pagina, int tamanho);

    void remover(Long id);
}
