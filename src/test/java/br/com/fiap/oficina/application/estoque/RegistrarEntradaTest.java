package br.com.fiap.oficina.application.estoque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.estoque.TipoItem;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class RegistrarEntradaTest {

    private final ItemEstoqueRepositoryEmMemoria itens = new ItemEstoqueRepositoryEmMemoria();
    private final CadastrarItem cadastrar = new CadastrarItem(itens);
    private final RegistrarEntrada registrar = new RegistrarEntrada(itens);

    @Test
    void entradaSomaAoSaldoFisico() {
        Long id = cadastrar.executar(new CadastrarItemDTO("A", "Filtro", TipoItem.PECA, BigDecimal.TEN, 10)).id();

        ItemEstoqueDTO dto = registrar.executar(new RegistrarEntradaDTO(id, 5));

        assertThat(dto.saldoFisico()).isEqualTo(15);
        assertThat(itens.buscarPorId(id).orElseThrow().getSaldoFisico()).isEqualTo(15);
    }

    @Test
    void quantidadeZeroGeraErroDeDominioSemAlterarSaldo() {
        Long id = cadastrar.executar(new CadastrarItemDTO("A", "Filtro", TipoItem.PECA, BigDecimal.TEN, 10)).id();

        assertThatThrownBy(() -> registrar.executar(new RegistrarEntradaDTO(id, 0)))
                .isInstanceOf(DomainException.class)
                .isNotInstanceOf(NaoEncontradoException.class);
        assertThat(itens.buscarPorId(id).orElseThrow().getSaldoFisico()).isEqualTo(10);
    }

    @Test
    void itemInexistenteGeraNaoEncontrado() {
        assertThatThrownBy(() -> registrar.executar(new RegistrarEntradaDTO(99L, 1)))
                .isInstanceOf(NaoEncontradoException.class);
    }
}
