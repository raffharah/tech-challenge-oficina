package br.com.fiap.oficina.application.estoque;

import br.com.fiap.oficina.domain.Pagina;
import br.com.fiap.oficina.domain.estoque.ItemEstoque;
import br.com.fiap.oficina.domain.estoque.ItemEstoqueRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class ItemEstoqueRepositoryEmMemoria implements ItemEstoqueRepository {

    private final Map<Long, ItemEstoque> armazenados = new LinkedHashMap<>();
    private long sequencia = 0;

    @Override
    public ItemEstoque salvar(ItemEstoque item) {
        Long id = item.getId() == null ? ++sequencia : item.getId();
        armazenados.put(id, copia(id, item));
        return copia(id, item);
    }

    @Override
    public Optional<ItemEstoque> buscarPorId(Long id) {
        return Optional.ofNullable(armazenados.get(id)).map(i -> copia(i.getId(), i));
    }

    @Override
    public List<ItemEstoque> buscarPorIds(Collection<Long> ids) {
        return armazenados.values().stream().filter(i -> ids.contains(i.getId())).map(i -> copia(i.getId(), i))
                .toList();
    }

    @Override
    public boolean existePorSku(String sku) {
        return armazenados.values().stream().anyMatch(i -> i.getSku().equalsIgnoreCase(sku));
    }

    @Override
    public Pagina<ItemEstoque> listar(int pagina, int tamanho) {
        List<ItemEstoque> todos = new ArrayList<>(armazenados.values());
        int de = Math.min(pagina * tamanho, todos.size());
        int ate = Math.min(de + tamanho, todos.size());
        return new Pagina<>(todos.subList(de, ate), pagina, tamanho, todos.size());
    }

    @Override
    public void remover(Long id) {
        armazenados.remove(id);
    }

    private static ItemEstoque copia(Long id, ItemEstoque item) {
        return new ItemEstoque(id, item.getSku(), item.getNome(), item.getTipo(), item.getPrecoVenda(),
                item.getSaldoFisico());
    }

    int quantidade() {
        return armazenados.size();
    }
}
