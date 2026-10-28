package br.com.fiap.oficina.application.estoque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.estoque.TipoItem;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class AtualizarItemTest {

    private final ItemEstoqueRepositoryEmMemoria itens = new ItemEstoqueRepositoryEmMemoria();
    private final CadastrarItem cadastrar = new CadastrarItem(itens);
    private final AtualizarItem atualizar = new AtualizarItem(itens);

    @Test
    void alteraNomeEPrecoSemMudarSaldo() {
        Long id = cadastrar.executar(new CadastrarItemDTO("FILTRO-01", "Filtro", TipoItem.PECA, BigDecimal.TEN, 8)).id();

        ItemEstoqueDTO dto = atualizar.executar(new AtualizarItemDTO(id, "Filtro de ar", new BigDecimal("55.50")));

        assertThat(dto.nome()).isEqualTo("Filtro de ar");
        assertThat(dto.precoVenda()).isEqualByComparingTo("55.50");
        assertThat(dto.saldoFisico()).isEqualTo(8);
        assertThat(itens.buscarPorId(id).orElseThrow().getSaldoFisico()).isEqualTo(8);
        assertThat(itens.buscarPorId(id).orElseThrow().getNome()).isEqualTo("Filtro de ar");
    }

    @Test
    void itemInexistenteGeraNaoEncontrado() {
        assertThatThrownBy(() -> atualizar.executar(new AtualizarItemDTO(99L, "Filtro", BigDecimal.TEN)))
                .isInstanceOf(NaoEncontradoException.class);
    }
}
