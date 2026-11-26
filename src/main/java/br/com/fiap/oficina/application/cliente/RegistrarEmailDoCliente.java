package br.com.fiap.oficina.application.cliente;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.cliente.Cliente;
import br.com.fiap.oficina.domain.cliente.ClienteRepository;
import br.com.fiap.oficina.domain.cliente.CpfCnpj;
import br.com.fiap.oficina.domain.cliente.Email;

public class RegistrarEmailDoCliente {

    private final ClienteRepository clientes;

    public RegistrarEmailDoCliente(ClienteRepository clientes) {
        this.clientes = clientes;
    }

    public ClienteDTO executar(String cpfCnpj, String email) {
        Cliente cliente = clientes.buscarPorCpfCnpj(new CpfCnpj(cpfCnpj))
                .orElseThrow(() -> new NaoEncontradoException("CLIENTE_NAO_ENCONTRADO", "Cliente nao encontrado"));
        cliente.registrarEmail(new Email(email));
        return ClienteDTO.de(clientes.salvar(cliente));
    }
}
