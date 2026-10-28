package br.com.fiap.oficina.domain.cliente;

import java.util.List;
import java.util.Optional;

public interface VeiculoRepository {

    Veiculo salvar(Veiculo veiculo);

    Optional<Veiculo> buscarPorId(Long id);

    Optional<Veiculo> buscarPorPlaca(Placa placa);

    List<Veiculo> listarPorCliente(Long clienteId);

    boolean existePorCliente(Long clienteId);

    void remover(Long id);
}
