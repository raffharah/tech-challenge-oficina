package br.com.fiap.oficina.application.cliente;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.cliente.VeiculoRepository;

public class RemoverVeiculo {

    private final VeiculoRepository veiculos;

    public RemoverVeiculo(VeiculoRepository veiculos) {
        this.veiculos = veiculos;
    }

    public void executar(Long id) {
        if (veiculos.buscarPorId(id).isEmpty()) {
            throw new NaoEncontradoException("VEICULO_NAO_ENCONTRADO", "Veiculo nao encontrado");
        }
        veiculos.remover(id);
    }
}
