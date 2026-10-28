package br.com.fiap.oficina.application.cliente;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.Pagina;
import br.com.fiap.oficina.domain.cliente.ClienteRepository;
import br.com.fiap.oficina.domain.cliente.CpfCnpj;

public class ConsultarClientes {

    private final ClienteRepository clientes;

    public ConsultarClientes(ClienteRepository clientes) {
        this.clientes = clientes;
    }

    public ClienteDTO buscarPorId(Long id) {
        return clientes.buscarPorId(id).map(ClienteDTO::de).orElseThrow(ConsultarClientes::naoEncontrado);
    }

    public ClienteDTO buscarPorCpfCnpj(String cpfCnpj) {
        return clientes.buscarPorCpfCnpj(new CpfCnpj(cpfCnpj)).map(ClienteDTO::de)
                .orElseThrow(ConsultarClientes::naoEncontrado);
    }

    public Pagina<ClienteDTO> listar(int pagina, int tamanho) {
        return clientes.listar(pagina, tamanho).map(ClienteDTO::de);
    }

    private static NaoEncontradoException naoEncontrado() {
        return new NaoEncontradoException("CLIENTE_NAO_ENCONTRADO", "Cliente nao encontrado");
    }
}
