package br.com.fiap.oficina.application.cliente;

import br.com.fiap.oficina.domain.ConflitoException;
import br.com.fiap.oficina.domain.cliente.Cliente;
import br.com.fiap.oficina.domain.cliente.ClienteRepository;
import br.com.fiap.oficina.domain.cliente.CpfCnpj;

public class CadastrarCliente {

    private final ClienteRepository clientes;

    public CadastrarCliente(ClienteRepository clientes) {
        this.clientes = clientes;
    }

    public ClienteDTO executar(CadastrarClienteDTO dto) {
        Cliente cliente = new Cliente(null, dto.nome(), new CpfCnpj(dto.cpfCnpj()));
        if (clientes.buscarPorCpfCnpj(cliente.getCpfCnpj()).isPresent()) {
            throw new ConflitoException("CLIENTE_DUPLICADO", "Ja existe cliente com este CPF/CNPJ");
        }
        return ClienteDTO.de(clientes.salvar(cliente));
    }
}
