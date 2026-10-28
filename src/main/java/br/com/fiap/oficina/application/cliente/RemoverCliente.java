package br.com.fiap.oficina.application.cliente;

import br.com.fiap.oficina.domain.ConflitoException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.cliente.ClienteRepository;
import br.com.fiap.oficina.domain.cliente.VeiculoRepository;

public class RemoverCliente {

    private final ClienteRepository clientes;
    private final VeiculoRepository veiculos;

    public RemoverCliente(ClienteRepository clientes, VeiculoRepository veiculos) {
        this.clientes = clientes;
        this.veiculos = veiculos;
    }

    public void executar(Long id) {
        if (clientes.buscarPorId(id).isEmpty()) {
            throw new NaoEncontradoException("CLIENTE_NAO_ENCONTRADO", "Cliente nao encontrado");
        }
        if (veiculos.existePorCliente(id)) {
            throw new ConflitoException("CLIENTE_COM_VEICULOS", "Cliente possui veiculos cadastrados");
        }
        clientes.remover(id);
    }
}
