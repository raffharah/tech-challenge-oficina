package br.com.fiap.oficina.infrastructure.db.estoque;

import br.com.fiap.oficina.domain.Pagina;
import br.com.fiap.oficina.domain.estoque.ItemEstoque;
import br.com.fiap.oficina.domain.estoque.ItemEstoqueRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class ItemEstoqueRepositoryImpl implements ItemEstoqueRepository {

    private final ItemEstoqueJpaRepository jpa;

    ItemEstoqueRepositoryImpl(ItemEstoqueJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ItemEstoque salvar(ItemEstoque item) {
        ItemEstoqueEntity entity = item.getId() == null
                ? new ItemEstoqueEntity()
                : jpa.findById(item.getId()).orElseThrow();
        entity.sku = item.getSku();
        entity.nome = item.getNome();
        entity.tipo = item.getTipo();
        entity.precoVenda = item.getPrecoVenda();
        entity.saldoFisico = item.getSaldoFisico();
        return paraModelo(jpa.saveAndFlush(entity));
    }

    @Override
    public Optional<ItemEstoque> buscarPorId(Long id) {
        return jpa.findById(id).map(ItemEstoqueRepositoryImpl::paraModelo);
    }

    @Override
    public List<ItemEstoque> buscarPorIds(Collection<Long> ids) {
        return jpa.findAllById(ids).stream().map(ItemEstoqueRepositoryImpl::paraModelo).toList();
    }

    @Override
    public boolean existePorSku(String sku) {
        return jpa.existsBySkuIgnoreCase(sku);
    }

    @Override
    public Pagina<ItemEstoque> listar(int pagina, int tamanho) {
        Page<ItemEstoqueEntity> page = jpa.findAll(PageRequest.of(pagina, tamanho, Sort.by("id")));
        return new Pagina<>(page.map(ItemEstoqueRepositoryImpl::paraModelo).getContent(), pagina, tamanho,
                page.getTotalElements());
    }

    @Override
    public void remover(Long id) {
        jpa.deleteById(id);
        jpa.flush();
    }

    private static ItemEstoque paraModelo(ItemEstoqueEntity entity) {
        return new ItemEstoque(entity.id, entity.sku, entity.nome, entity.tipo, entity.precoVenda,
                entity.saldoFisico);
    }
}
