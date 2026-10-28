package br.com.fiap.oficina.application.cliente;

import br.com.fiap.oficina.domain.cliente.Cliente;

public record ClienteDTO(Long id, String nome, String cpfCnpj) {

    static ClienteDTO de(Cliente cliente) {
        return new ClienteDTO(cliente.getId(), cliente.getNome(), cliente.getCpfCnpj().valor());
    }
}
