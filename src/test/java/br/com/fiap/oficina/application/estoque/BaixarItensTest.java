package br.com.fiap.oficina.application.estoque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.estoque.TipoItem;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class BaixarItensTest {

    private final ItemEstoqueRepositoryEmMemoria itens = new ItemEstoqueRepositoryEmMemoria();
    private final CadastrarItem cadastrar = new CadastrarItem(itens);
    private final BaixarItens baixar = new BaixarItens(itens);

    private Long novoItem(String sku, int saldo) {
        return cadastrar.executar(new CadastrarItemDTO(sku, "Item " + sku, TipoItem.PECA, BigDecimal.TEN, saldo)).id();
    }

    private int saldo(Long id) {
        return itens.buscarPorId(id).orElseThrow().getSaldoFisico();
    }

    @Test
    void baixaTodosOsItensComSaldo() {
        Long a = novoItem("A", 10);
        Long b = novoItem("B", 5);

        baixar.executar(List.of(new ItemBaixaDTO(a, 4), new ItemBaixaDTO(b, 5)));

        assertThat(saldo(a)).isEqualTo(6);
        assertThat(saldo(b)).isZero();
    }

    @Test
    void umFaltanteNaoBaixaNenhumEListaSoOFaltante() {
        Long a = novoItem("A", 10);
        Long b = novoItem("B", 1);

        assertThatThrownBy(() -> baixar.executar(List.of(new ItemBaixaDTO(a, 4), new ItemBaixaDTO(b, 3))))
                .isInstanceOfSatisfying(DomainException.class, e -> {
                    assertThat(e.getCodigo()).isEqualTo("SALDO_INSUFICIENTE");
                    assertThat(e.getDetalhes().toArray()).containsExactly(new FaltaDeSaldoDTO("B", 3, 1));
                });
        assertThat(saldo(a)).isEqualTo(10);
        assertThat(saldo(b)).isEqualTo(1);
    }

    @Test
    void listaTodosOsFaltantes() {
        Long a = novoItem("A", 1);
        Long b = novoItem("B", 2);

        assertThatThrownBy(() -> baixar.executar(List.of(new ItemBaixaDTO(a, 2), new ItemBaixaDTO(b, 5))))
                .isInstanceOfSatisfying(DomainException.class, e -> assertThat(e.getDetalhes().toArray())
                        .containsExactly(new FaltaDeSaldoDTO("A", 2, 1), new FaltaDeSaldoDTO("B", 5, 2)));
    }

    @Test
    void mesmoItemRepetidoSomaQuantidadesAntesDeVerificar() {
        Long a = novoItem("A", 3);

        assertThatThrownBy(() -> baixar.executar(List.of(new ItemBaixaDTO(a, 2), new ItemBaixaDTO(a, 2))))
                .isInstanceOfSatisfying(DomainException.class, e -> assertThat(e.getDetalhes().toArray())
                        .containsExactly(new FaltaDeSaldoDTO("A", 4, 3)));
        assertThat(saldo(a)).isEqualTo(3);
    }

    @Test
    void ultimaUnidadeBaixadaUmaVezSegundaFalhaESaldoFicaZero() {
        Long a = novoItem("A", 1);

        baixar.executar(List.of(new ItemBaixaDTO(a, 1)));

        assertThatThrownBy(() -> baixar.executar(List.of(new ItemBaixaDTO(a, 1)))).isInstanceOf(DomainException.class);
        assertThat(saldo(a)).isZero();
    }

    @Test
    void itemInexistenteGeraNaoEncontradoSemBaixarOutros() {
        Long a = novoItem("A", 10);

        assertThatThrownBy(() -> baixar.executar(List.of(new ItemBaixaDTO(a, 1), new ItemBaixaDTO(99L, 1))))
                .isInstanceOf(NaoEncontradoException.class);
        assertThat(saldo(a)).isEqualTo(10);
    }
}
