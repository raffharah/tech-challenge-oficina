package br.com.fiap.oficina.domain.estoque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ItemEstoqueTest {

    private static ItemEstoque item(int saldo) {
        return new ItemEstoque(1L, "OLEO-5W30", "Oleo 5W30", TipoItem.INSUMO, new BigDecimal("35.90"), saldo);
    }

    @Test
    void criaItemComSaldoFisicoIgualAoInicial() {
        ItemEstoque item = new ItemEstoque(null, " OLEO-5W30 ", " Oleo 5W30 ", TipoItem.INSUMO,
                new BigDecimal("35.90"), 10);

        assertThat(item.getSku()).isEqualTo("OLEO-5W30");
        assertThat(item.getNome()).isEqualTo("Oleo 5W30");
        assertThat(item.getTipo()).isEqualTo(TipoItem.INSUMO);
        assertThat(item.getPrecoVenda()).isEqualByComparingTo("35.90");
        assertThat(item.getSaldoFisico()).isEqualTo(10);
    }

    @Test
    void precoZeroNegativoOuNuloRejeitado() {
        assertThatThrownBy(() -> new ItemEstoque(null, "A", "Item", TipoItem.PECA, BigDecimal.ZERO, 0))
                .isInstanceOf(DomainException.class)
                .extracting("codigo").isEqualTo("PRECO_VENDA_INVALIDO");
        assertThatThrownBy(() -> new ItemEstoque(null, "A", "Item", TipoItem.PECA, new BigDecimal("-0.01"), 0))
                .isInstanceOf(DomainException.class)
                .extracting("codigo").isEqualTo("PRECO_VENDA_INVALIDO");
        assertThatThrownBy(() -> new ItemEstoque(null, "A", "Item", TipoItem.PECA, null, 0))
                .isInstanceOf(DomainException.class)
                .extracting("codigo").isEqualTo("PRECO_VENDA_INVALIDO");
    }

    @Test
    void saldoInicialNegativoRejeitado() {
        assertThatThrownBy(() -> item(-1))
                .isInstanceOf(DomainException.class)
                .extracting("codigo").isEqualTo("SALDO_INICIAL_INVALIDO");
    }

    @Test
    void skuNomeETipoObrigatorios() {
        assertThatThrownBy(() -> new ItemEstoque(null, " ", "Item", TipoItem.PECA, BigDecimal.TEN, 0))
                .isInstanceOf(DomainException.class)
                .extracting("codigo").isEqualTo("SKU_OBRIGATORIO");
        assertThatThrownBy(() -> new ItemEstoque(null, "A", null, TipoItem.PECA, BigDecimal.TEN, 0))
                .isInstanceOf(DomainException.class)
                .extracting("codigo").isEqualTo("NOME_OBRIGATORIO");
        assertThatThrownBy(() -> new ItemEstoque(null, "A", "Item", null, BigDecimal.TEN, 0))
                .isInstanceOf(DomainException.class)
                .extracting("codigo").isEqualTo("TIPO_OBRIGATORIO");
    }

    @Test
    void atualizarAlteraNomeEPrecoSemMudarSaldo() {
        ItemEstoque item = item(7);

        item.atualizar("Oleo 5W40", new BigDecimal("40.00"));

        assertThat(item.getNome()).isEqualTo("Oleo 5W40");
        assertThat(item.getPrecoVenda()).isEqualByComparingTo("40.00");
        assertThat(item.getSaldoFisico()).isEqualTo(7);
    }

    @Test
    void atualizarComDadosInvalidosRejeitadoMantendoEstado() {
        ItemEstoque item = item(7);

        assertThatThrownBy(() -> item.atualizar("Outro", BigDecimal.ZERO))
                .isInstanceOf(DomainException.class)
                .extracting("codigo").isEqualTo("PRECO_VENDA_INVALIDO");
        assertThatThrownBy(() -> item.atualizar(" ", BigDecimal.ONE))
                .isInstanceOf(DomainException.class)
                .extracting("codigo").isEqualTo("NOME_OBRIGATORIO");
        assertThat(item.getNome()).isEqualTo("Oleo 5W30");
        assertThat(item.getPrecoVenda()).isEqualByComparingTo("35.90");
    }

    @Test
    void entradaSomaAoSaldoFisico() {
        ItemEstoque item = item(10);

        item.registrarEntrada(5);

        assertThat(item.getSaldoFisico()).isEqualTo(15);
    }

    @Test
    void entradaZeroOuNegativaRejeitada() {
        ItemEstoque item = item(10);

        assertThatThrownBy(() -> item.registrarEntrada(0))
                .isInstanceOf(DomainException.class)
                .extracting("codigo").isEqualTo("QUANTIDADE_INVALIDA");
        assertThatThrownBy(() -> item.registrarEntrada(-3))
                .isInstanceOf(DomainException.class)
                .extracting("codigo").isEqualTo("QUANTIDADE_INVALIDA");
        assertThat(item.getSaldoFisico()).isEqualTo(10);
    }

    @Test
    void baixaSubtraiDoSaldoFisico() {
        ItemEstoque item = item(10);

        item.baixar(4);

        assertThat(item.getSaldoFisico()).isEqualTo(6);
    }

    @Test
    void baixaExataZeraSaldo() {
        ItemEstoque item = item(3);

        item.baixar(3);

        assertThat(item.getSaldoFisico()).isZero();
    }

    @Test
    void baixaAcimaDoSaldoRejeitadaSemAlterar() {
        ItemEstoque item = item(2);

        assertThatThrownBy(() -> item.baixar(3))
                .isInstanceOf(DomainException.class)
                .extracting("codigo").isEqualTo("SALDO_INSUFICIENTE");
        assertThat(item.getSaldoFisico()).isEqualTo(2);
    }

    @Test
    void baixaComQuantidadeZeroOuNegativaRejeitada() {
        ItemEstoque item = item(2);

        assertThatThrownBy(() -> item.baixar(0))
                .isInstanceOf(DomainException.class)
                .extracting("codigo").isEqualTo("QUANTIDADE_INVALIDA");
        assertThat(item.getSaldoFisico()).isEqualTo(2);
    }
}
