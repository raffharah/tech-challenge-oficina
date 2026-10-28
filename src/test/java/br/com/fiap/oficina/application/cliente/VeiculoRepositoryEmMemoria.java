package br.com.fiap.oficina.application.cliente;

import br.com.fiap.oficina.domain.cliente.Placa;
import br.com.fiap.oficina.domain.cliente.Veiculo;
import br.com.fiap.oficina.domain.cliente.VeiculoRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class VeiculoRepositoryEmMemoria implements VeiculoRepository {

    private final Map<Long, Veiculo> armazenados = new LinkedHashMap<>();
    private long sequencia = 0;

    @Override
    public Veiculo salvar(Veiculo veiculo) {
        Veiculo salvo = veiculo.getId() == null
                ? new Veiculo(++sequencia, veiculo.getPlaca(), veiculo.getMarca(), veiculo.getModelo(),
                        veiculo.getAno(), veiculo.getClienteId(), veiculo.getAno())
                : veiculo;
        armazenados.put(salvo.getId(), salvo);
        return salvo;
    }

    @Override
    public Optional<Veiculo> buscarPorId(Long id) {
        return Optional.ofNullable(armazenados.get(id));
    }

    @Override
    public Optional<Veiculo> buscarPorPlaca(Placa placa) {
        return armazenados.values().stream().filter(v -> v.getPlaca().equals(placa)).findFirst();
    }

    @Override
    public List<Veiculo> listarPorCliente(Long clienteId) {
        return armazenados.values().stream().filter(v -> v.getClienteId().equals(clienteId)).toList();
    }

    @Override
    public boolean existePorCliente(Long clienteId) {
        return armazenados.values().stream().anyMatch(v -> v.getClienteId().equals(clienteId));
    }

    @Override
    public void remover(Long id) {
        armazenados.remove(id);
    }

    int quantidade() {
        return armazenados.size();
    }
}
