package br.com.fiap.oficina.infrastructure.db.estoque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.Pagina;
import br.com.fiap.oficina.domain.estoque.ItemEstoque;
import br.com.fiap.oficina.domain.estoque.ItemEstoqueRepository;
import br.com.fiap.oficina.domain.estoque.TipoItem;
import br.com.fiap.oficina.infrastructure.TestcontainersConfig;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@Import(TestcontainersConfig.class)
class ItemEstoqueRepositoryImplIT {

    @Autowired
    ItemEstoqueRepository itens;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PlatformTransactionManager transactionManager;

    @BeforeEach
    void limparBanco() {
        jdbc.execute("truncate table item_estoque restart identity cascade");
    }

    private ItemEstoque novo(String sku, int saldo) {
        return itens.salvar(new ItemEstoque(null, sku, "Item " + sku, TipoItem.PECA, new BigDecimal("10.00"), saldo));
    }

    @Test
    void salvaEBuscaPorId() {
        ItemEstoque salvo = itens.salvar(
                new ItemEstoque(null, "OLEO-01", "Oleo", TipoItem.INSUMO, new BigDecimal("35.90"), 12));

        assertThat(salvo.getId()).isNotNull();
        ItemEstoque buscado = itens.buscarPorId(salvo.getId()).orElseThrow();
        assertThat(buscado.getSku()).isEqualTo("OLEO-01");
        assertThat(buscado.getNome()).isEqualTo("Oleo");
        assertThat(buscado.getTipo()).isEqualTo(TipoItem.INSUMO);
        assertThat(buscado.getPrecoVenda()).isEqualByComparingTo("35.90");
        assertThat(buscado.getSaldoFisico()).isEqualTo(12);
        assertThat(itens.buscarPorId(999L)).isEmpty();
    }

    @Test
    void alteraMantendoIdESku() {
        ItemEstoque salvo = novo("A", 5);
        salvo.atualizar("Novo nome", new BigDecimal("22.00"));
        salvo.registrarEntrada(3);

        ItemEstoque alterado = itens.salvar(salvo);

        assertThat(alterado.getId()).isEqualTo(salvo.getId());
        ItemEstoque buscado = itens.buscarPorId(salvo.getId()).orElseThrow();
        assertThat(buscado.getSku()).isEqualTo("A");
        assertThat(buscado.getNome()).isEqualTo("Novo nome");
        assertThat(buscado.getPrecoVenda()).isEqualByComparingTo("22.00");
        assertThat(buscado.getSaldoFisico()).isEqualTo(8);
    }

    @Test
    void buscaPorIdsRetornaSoOsExistentes() {
        ItemEstoque a = novo("A", 1);
        ItemEstoque b = novo("B", 2);
        novo("C", 3);

        List<ItemEstoque> encontrados = itens.buscarPorIds(List.of(a.getId(), b.getId(), 999L));

        assertThat(encontrados).extracting(ItemEstoque::getSku).containsExactlyInAnyOrder("A", "B");
    }

    @Test
    void existePorSkuIgnoraCaixa() {
        novo("Filtro-01", 1);

        assertThat(itens.existePorSku("FILTRO-01")).isTrue();
        assertThat(itens.existePorSku("OUTRO")).isFalse();
    }

    @Test
    void listaPaginado() {
        novo("A", 1);
        novo("B", 1);
        novo("C", 1);

        Pagina<ItemEstoque> pagina = itens.listar(1, 2);

        assertThat(pagina.itens()).extracting(ItemEstoque::getSku).containsExactly("C");
        assertThat(pagina.total()).isEqualTo(3);
        assertThat(pagina.tamanho()).isEqualTo(2);
    }

    @Test
    void removeItem() {
        ItemEstoque salvo = novo("A", 1);

        itens.remover(salvo.getId());

        assertThat(itens.buscarPorId(salvo.getId())).isEmpty();
    }

    @Test
    void skuDuplicadoEmOutraCaixaViolaConstraint() {
        novo("Filtro-01", 1);

        assertThatThrownBy(() -> novo("FILTRO-01", 1)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void precoNaoPositivoOuSaldoNegativoViolaConstraint() {
        assertThatThrownBy(() -> jdbc.update(
                "insert into item_estoque (sku, nome, tipo, preco_venda, saldo_fisico) values ('X', 'X', 'PECA', 0, 1)"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update(
                "insert into item_estoque (sku, nome, tipo, preco_venda, saldo_fisico) values ('Y', 'Y', 'PECA', 1, -1)"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void baixasConcorrentesDoUltimoItemConfirmamUmaEFalhamOutra() {
        Long id = novo("ULTIMO", 1).getId();
        CyclicBarrier lidos = new CyclicBarrier(2);
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        List<CompletableFuture<Void>> baixas = List.of(1, 2).stream()
                .map(n -> CompletableFuture.runAsync(() -> tx.executeWithoutResult(status -> {
                    ItemEstoque item = itens.buscarPorId(id).orElseThrow();
                    await(lidos);
                    item.baixar(1);
                    itens.salvar(item);
                })))
                .toList();

        long falhas = baixas.stream().filter(f -> {
            try {
                f.get();
                return false;
            } catch (ExecutionException e) {
                assertThat(e.getCause()).isInstanceOf(ObjectOptimisticLockingFailureException.class);
                return true;
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }).count();

        assertThat(falhas).isEqualTo(1);
        assertThat(itens.buscarPorId(id).orElseThrow().getSaldoFisico()).isZero();
    }

    private static void await(CyclicBarrier barreira) {
        try {
            barreira.await();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
