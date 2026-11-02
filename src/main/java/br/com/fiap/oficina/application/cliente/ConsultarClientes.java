package br.com.fiap.oficina.application.cliente;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.Pagina;
import br.com.fiap.oficina.domain.cliente.ClienteRepository;
import br.com.fiap.oficina.domain.cliente.CpfCnpj;
import java.util.List;

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

    // cpfCnpj é filtro opcional: sem ele lista todos; com ele devolve a página com 0 ou 1 cliente
    public Pagina<ClienteDTO> listar(String cpfCnpj, int pagina, int tamanho) {
        if (cpfCnpj == null || cpfCnpj.isBlank()) {
            return clientes.listar(pagina, tamanho).map(ClienteDTO::de);
        }
        List<ClienteDTO> encontrados = clientes.buscarPorCpfCnpj(new CpfCnpj(cpfCnpj)).map(ClienteDTO::de)
                .stream().toList();
        return Pagina.de(encontrados, pagina, tamanho);
    }

    private static NaoEncontradoException naoEncontrado() {
        return new NaoEncontradoException("CLIENTE_NAO_ENCONTRADO", "Cliente nao encontrado");
    }
}
