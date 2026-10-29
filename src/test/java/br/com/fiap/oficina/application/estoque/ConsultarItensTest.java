package br.com.fiap.oficina.application.estoque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.Pagina;
import br.com.fiap.oficina.domain.estoque.TipoItem;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ConsultarItensTest {

    private final ItemEstoqueRepositoryEmMemoria itens = new ItemEstoqueRepositoryEmMemoria();
    private final CadastrarItem cadastrar = new CadastrarItem(itens);
    private final ConsultarItens consultar = new ConsultarItens(itens);

    @Test
    void buscaRetornaTipoPrecoESaldo() {
        Long id = cadastrar.executar(
                new CadastrarItemDTO("OLEO-01", "Oleo", TipoItem.INSUMO, new BigDecimal("30.00"), 12)).id();

        ItemEstoqueDTO dto = consultar.buscarPorId(id);

        assertThat(dto.tipo()).isEqualTo(TipoItem.INSUMO);
        assertThat(dto.precoVenda()).isEqualByComparingTo("30.00");
        assertThat(dto.saldoFisico()).isEqualTo(12);
    }

    @Test
    void listagemRetornaTipoPrecoESaldoPaginados() {
        cadastrar.executar(new CadastrarItemDTO("A", "Filtro", TipoItem.PECA, new BigDecimal("45.00"), 3));
        cadastrar.executar(new CadastrarItemDTO("B", "Oleo", TipoItem.INSUMO, new BigDecimal("30.00"), 12));
        cadastrar.executar(new CadastrarItemDTO("C", "Vela", TipoItem.PECA, new BigDecimal("20.00"), 0));

        Pagina<ItemEstoqueDTO> pagina = consultar.listar(0, 2);

        assertThat(pagina.total()).isEqualTo(3);
        assertThat(pagina.itens()).hasSize(2);
        assertThat(pagina.itens().get(0).tipo()).isEqualTo(TipoItem.PECA);
        assertThat(pagina.itens().get(0).precoVenda()).isEqualByComparingTo("45.00");
        assertThat(pagina.itens().get(0).saldoFisico()).isEqualTo(3);
        assertThat(pagina.itens().get(1).tipo()).isEqualTo(TipoItem.INSUMO);
        assertThat(pagina.itens().get(1).saldoFisico()).isEqualTo(12);
    }

    @Test
    void itemInexistenteGeraNaoEncontrado() {
        assertThatThrownBy(() -> consultar.buscarPorId(99L)).isInstanceOf(NaoEncontradoException.class);
    }
}
