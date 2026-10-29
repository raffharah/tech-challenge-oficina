package br.com.fiap.oficina.application.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.ordemservico.ItemCotado;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RemoverItemDaOrdemTest {

    private final OrdemServicoRepositoryEmMemoria ordens = new OrdemServicoRepositoryEmMemoria();
    private final IncluirItemNaOrdem incluir = new IncluirItemNaOrdem(ordens,
            new EstoqueDeItensEmMemoria(Map.of(10L, new ItemCotado(10L, "OLE-1", "Oleo", new BigDecimal("35.50")))));
    private final RemoverItemDaOrdem remover = new RemoverItemDaOrdem(ordens);

    @Test
    void removeItemDaOrdem() {
        Long ordemId = OrdemServicoEmDiagnostico.salvarEm(ordens);
        incluir.executar(ordemId, 10L, 2);

        remover.executar(ordemId, 10L);

        assertThat(ordens.buscarPorId(ordemId).orElseThrow().getItens()).isEmpty();
    }

    @Test
    void ordemForaDeDiagnosticoLancaDomainException() {
        Long ordemId = OrdemServicoEmDiagnostico.salvarEm(ordens);
        incluir.executar(ordemId, 10L, 2);
        ServicoRepositoryEmMemoria servicos = new ServicoRepositoryEmMemoria();
        Long servicoId = new CadastrarServico(servicos).executar(new CadastrarServicoDTO("A", BigDecimal.TEN)).id();
        new IncluirServicoNaOrdem(ordens, servicos).executar(ordemId, servicoId);
        new ConcluirDiagnostico(ordens, Clock.systemUTC()).executar(ordemId);

        assertThatThrownBy(() -> remover.executar(ordemId, 10L)).isInstanceOf(DomainException.class);
        assertThat(ordens.buscarPorId(ordemId).orElseThrow().getItens()).hasSize(1);
    }
}
