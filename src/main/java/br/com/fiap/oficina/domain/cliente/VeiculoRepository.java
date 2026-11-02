package br.com.fiap.oficina.domain.cliente;

import br.com.fiap.oficina.domain.Pagina;
import java.util.List;
import java.util.Optional;

public interface VeiculoRepository {

    Veiculo salvar(Veiculo veiculo);

    Optional<Veiculo> buscarPorId(Long id);

    Optional<Veiculo> buscarPorPlaca(Placa placa);

    Pagina<Veiculo> listar(int pagina, int tamanho);

    List<Veiculo> listarPorCliente(Long clienteId);

    boolean existePorCliente(Long clienteId);

    void remover(Long id);
}
