package br.com.fiap.oficina.application.cliente;

import br.com.fiap.oficina.domain.Pagina;
import br.com.fiap.oficina.domain.cliente.Cliente;
import br.com.fiap.oficina.domain.cliente.ClienteRepository;
import br.com.fiap.oficina.domain.cliente.CpfCnpj;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class ClienteRepositoryEmMemoria implements ClienteRepository {

    private final Map<Long, Cliente> armazenados = new LinkedHashMap<>();
    private long sequencia = 0;

    @Override
    public Cliente salvar(Cliente cliente) {
        Cliente salvo = cliente.getId() == null
                ? new Cliente(++sequencia, cliente.getNome(), cliente.getCpfCnpj())
                : cliente;
        armazenados.put(salvo.getId(), salvo);
        return salvo;
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return Optional.ofNullable(armazenados.get(id));
    }

    @Override
    public Optional<Cliente> buscarPorCpfCnpj(CpfCnpj cpfCnpj) {
        return armazenados.values().stream().filter(c -> c.getCpfCnpj().equals(cpfCnpj)).findFirst();
    }

    @Override
    public Pagina<Cliente> listar(int pagina, int tamanho) {
        List<Cliente> todos = new ArrayList<>(armazenados.values());
        int de = Math.min(pagina * tamanho, todos.size());
        int ate = Math.min(de + tamanho, todos.size());
        return new Pagina<>(todos.subList(de, ate), pagina, tamanho, todos.size());
    }

    @Override
    public void remover(Long id) {
        armazenados.remove(id);
    }

    int quantidade() {
        return armazenados.size();
    }
}
