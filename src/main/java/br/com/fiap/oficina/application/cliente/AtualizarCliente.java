package br.com.fiap.oficina.application.cliente;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.cliente.Cliente;
import br.com.fiap.oficina.domain.cliente.ClienteRepository;

public class AtualizarCliente {

    private final ClienteRepository clientes;

    public AtualizarCliente(ClienteRepository clientes) {
        this.clientes = clientes;
    }

    public ClienteDTO executar(AtualizarClienteDTO dto) {
        Cliente cliente = clientes.buscarPorId(dto.id())
                .orElseThrow(() -> new NaoEncontradoException("CLIENTE_NAO_ENCONTRADO", "Cliente nao encontrado"));
        cliente.renomear(dto.nome());
        return ClienteDTO.de(clientes.salvar(cliente));
    }
}
