package br.com.fiap.oficina.application.estoque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.estoque.TipoItem;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class RemoverItemTest {

    private final ItemEstoqueRepositoryEmMemoria itens = new ItemEstoqueRepositoryEmMemoria();
    private final CadastrarItem cadastrar = new CadastrarItem(itens);
    private final RemoverItem remover = new RemoverItem(itens);

    @Test
    void removeItemExistente() {
        Long id = cadastrar.executar(new CadastrarItemDTO("A", "Filtro", TipoItem.PECA, BigDecimal.TEN, 1)).id();

        remover.executar(id);

        assertThat(itens.quantidade()).isZero();
    }

    @Test
    void itemInexistenteGeraNaoEncontrado() {
        assertThatThrownBy(() -> remover.executar(99L)).isInstanceOf(NaoEncontradoException.class);
    }
}
