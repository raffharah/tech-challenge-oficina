package br.com.fiap.oficina.application.estoque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.ConflitoException;
import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.estoque.TipoItem;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CadastrarItemTest {

    private final ItemEstoqueRepositoryEmMemoria itens = new ItemEstoqueRepositoryEmMemoria();
    private final CadastrarItem cadastrar = new CadastrarItem(itens);

    @Test
    void cadastraItemComSaldoFisicoIgualAoInicial() {
        ItemEstoqueDTO dto = cadastrar.executar(
                new CadastrarItemDTO("FILTRO-01", "Filtro de oleo", TipoItem.PECA, new BigDecimal("45.00"), 8));

        assertThat(dto.id()).isNotNull();
        assertThat(dto.sku()).isEqualTo("FILTRO-01");
        assertThat(dto.tipo()).isEqualTo(TipoItem.PECA);
        assertThat(dto.precoVenda()).isEqualByComparingTo("45.00");
        assertThat(dto.saldoFisico()).isEqualTo(8);
        assertThat(itens.quantidade()).isEqualTo(1);
    }

    @Test
    void skuRepetidoGeraConflito() {
        cadastrar.executar(new CadastrarItemDTO("FILTRO-01", "Filtro", TipoItem.PECA, BigDecimal.TEN, 1));

        assertThatThrownBy(() -> cadastrar
                .executar(new CadastrarItemDTO("FILTRO-01", "Outro", TipoItem.PECA, BigDecimal.ONE, 5)))
                .isInstanceOf(ConflitoException.class);
        assertThat(itens.quantidade()).isEqualTo(1);
    }

    @Test
    void precoInvalidoOuSaldoInicialNegativoNaoPersiste() {
        assertThatThrownBy(() -> cadastrar
                .executar(new CadastrarItemDTO("A", "Item", TipoItem.INSUMO, BigDecimal.ZERO, 1)))
                .isInstanceOf(DomainException.class)
                .isNotInstanceOf(ConflitoException.class);
        assertThatThrownBy(() -> cadastrar
                .executar(new CadastrarItemDTO("B", "Item", TipoItem.INSUMO, BigDecimal.TEN, -1)))
                .isInstanceOf(DomainException.class)
                .isNotInstanceOf(ConflitoException.class);
        assertThat(itens.quantidade()).isZero();
    }
}
