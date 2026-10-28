package br.com.fiap.oficina.infrastructure.db.cliente;

import br.com.fiap.oficina.domain.cliente.Placa;
import br.com.fiap.oficina.domain.cliente.Veiculo;
import br.com.fiap.oficina.domain.cliente.VeiculoRepository;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class VeiculoRepositoryImpl implements VeiculoRepository {

    private final VeiculoJpaRepository jpa;

    VeiculoRepositoryImpl(VeiculoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Veiculo salvar(Veiculo veiculo) {
        VeiculoEntity entity = new VeiculoEntity();
        entity.id = veiculo.getId();
        entity.placa = veiculo.getPlaca().valor();
        entity.marca = veiculo.getMarca();
        entity.modelo = veiculo.getModelo();
        entity.ano = veiculo.getAno();
        entity.clienteId = veiculo.getClienteId();
        return paraModelo(jpa.saveAndFlush(entity));
    }

    @Override
    public Optional<Veiculo> buscarPorId(Long id) {
        return jpa.findById(id).map(VeiculoRepositoryImpl::paraModelo);
    }

    @Override
    public Optional<Veiculo> buscarPorPlaca(Placa placa) {
        return jpa.findByPlaca(placa.valor()).map(VeiculoRepositoryImpl::paraModelo);
    }

    @Override
    public List<Veiculo> listarPorCliente(Long clienteId) {
        return jpa.findByClienteIdOrderById(clienteId).stream().map(VeiculoRepositoryImpl::paraModelo).toList();
    }

    @Override
    public boolean existePorCliente(Long clienteId) {
        return jpa.existsByClienteId(clienteId);
    }

    @Override
    public void remover(Long id) {
        jpa.deleteById(id);
        jpa.flush();
    }

    // ponytail: ano ja persistido; reconstroi com limite maximo para nao revalidar contra o relogio
    private static Veiculo paraModelo(VeiculoEntity entity) {
        return new Veiculo(entity.id, new Placa(entity.placa), entity.marca, entity.modelo, entity.ano,
                entity.clienteId, Math.max(entity.ano, Year.now().getValue()));
    }
}
