package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.Pagina;
import br.com.fiap.oficina.domain.ordemservico.Servico;
import br.com.fiap.oficina.domain.ordemservico.ServicoRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class ServicoRepositoryEmMemoria implements ServicoRepository {

    private final Map<Long, Servico> armazenados = new LinkedHashMap<>();
    private long sequencia = 0;

    @Override
    public Servico salvar(Servico servico) {
        Long id = servico.getId() == null ? ++sequencia : servico.getId();
        armazenados.put(id, new Servico(id, servico.getNome(), servico.getValorPadrao()));
        return copia(armazenados.get(id));
    }

    @Override
    public Optional<Servico> buscarPorId(Long id) {
        return Optional.ofNullable(armazenados.get(id)).map(ServicoRepositoryEmMemoria::copia);
    }

    @Override
    public Optional<Servico> buscarPorNome(String nome) {
        return armazenados.values().stream().filter(s -> s.getNome().equalsIgnoreCase(nome)).findFirst()
                .map(ServicoRepositoryEmMemoria::copia);
    }

    @Override
    public Pagina<Servico> listar(int pagina, int tamanho) {
        List<Servico> todos = new ArrayList<>(armazenados.values());
        int de = Math.min(pagina * tamanho, todos.size());
        int ate = Math.min(de + tamanho, todos.size());
        return new Pagina<>(todos.subList(de, ate), pagina, tamanho, todos.size());
    }

    @Override
    public void remover(Long id) {
        armazenados.remove(id);
    }

    private static Servico copia(Servico servico) {
        return new Servico(servico.getId(), servico.getNome(), servico.getValorPadrao());
    }

    int quantidade() {
        return armazenados.size();
    }
}
