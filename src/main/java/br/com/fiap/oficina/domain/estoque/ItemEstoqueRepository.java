package br.com.fiap.oficina.domain.estoque;

import br.com.fiap.oficina.domain.Pagina;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ItemEstoqueRepository {

    ItemEstoque salvar(ItemEstoque item);

    Optional<ItemEstoque> buscarPorId(Long id);

    List<ItemEstoque> buscarPorIds(Collection<Long> ids);

    boolean existePorSku(String sku);

    Pagina<ItemEstoque> listar(int pagina, int tamanho);

    void remover(Long id);
}
