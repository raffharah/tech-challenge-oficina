package br.com.fiap.oficina.domain.cliente;

import br.com.fiap.oficina.domain.Pagina;
import java.util.Optional;

public interface ClienteRepository {

    Cliente salvar(Cliente cliente);

    Optional<Cliente> buscarPorId(Long id);

    Optional<Cliente> buscarPorCpfCnpj(CpfCnpj cpfCnpj);

    Pagina<Cliente> listar(int pagina, int tamanho);

    void remover(Long id);
}
