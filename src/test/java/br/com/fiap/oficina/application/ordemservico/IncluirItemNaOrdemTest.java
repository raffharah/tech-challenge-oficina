package br.com.fiap.oficina.application.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.ordemservico.EstoqueDeItens;
import br.com.fiap.oficina.domain.ordemservico.ItemCotado;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

class IncluirItemNaOrdemTest {

    private final OrdemServicoRepositoryEmMemoria ordens = new OrdemServicoRepositoryEmMemoria();
    private final Map<Long, ItemCotado> catalogo = Map.of(
            10L, new ItemCotado(10L, "OLE-1", "Oleo 5W30", new BigDecimal("35.50")));
    private final EstoqueDeItens estoque = new EstoqueDeItensEmMemoria(catalogo);
    private final IncluirItemNaOrdem incluir = new IncluirItemNaOrdem(ordens, estoque);

    @Test
    void incluiItemComPrecoCotado() {
        Long ordemId = OrdemServicoEmDiagnostico.salvarEm(ordens);

        OrdemServicoDTO dto = incluir.executar(ordemId, 10L, 4);

        assertThat(dto.itens()).containsExactly(
                new OrdemServicoDTO.ItemDaOrdemDTO(10L, "OLE-1", "Oleo 5W30", 4, new BigDecimal("35.50")));
        assertThat(ordens.buscarPorId(ordemId).orElseThrow().getItens()).hasSize(1);
    }

    @Test
    void itemInexistenteLancaDomainException() {
        Long ordemId = OrdemServicoEmDiagnostico.salvarEm(ordens);

        assertThatThrownBy(() -> incluir.executar(ordemId, 99L, 1)).isInstanceOf(DomainException.class);
        assertThat(ordens.buscarPorId(ordemId).orElseThrow().getItens()).isEmpty();
    }
}
