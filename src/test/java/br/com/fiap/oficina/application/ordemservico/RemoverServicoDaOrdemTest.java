package br.com.fiap.oficina.application.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import java.math.BigDecimal;
import java.time.Clock;
import org.junit.jupiter.api.Test;

class RemoverServicoDaOrdemTest {

    private final OrdemServicoRepositoryEmMemoria ordens = new OrdemServicoRepositoryEmMemoria();
    private final ServicoRepositoryEmMemoria servicos = new ServicoRepositoryEmMemoria();
    private final RemoverServicoDaOrdem remover = new RemoverServicoDaOrdem(ordens);

    private Long servicoNaOrdem(Long ordemId) {
        Long servicoId = new CadastrarServico(servicos).executar(new CadastrarServicoDTO("Alinhamento", BigDecimal.TEN))
                .id();
        new IncluirServicoNaOrdem(ordens, servicos).executar(ordemId, servicoId);
        return servicoId;
    }

    @Test
    void removeServicoDaOrdem() {
        Long ordemId = OrdemServicoEmDiagnostico.salvarEm(ordens);
        Long servicoId = servicoNaOrdem(ordemId);

        remover.executar(ordemId, servicoId);

        assertThat(ordens.buscarPorId(ordemId).orElseThrow().getServicos()).isEmpty();
    }

    @Test
    void ordemForaDeDiagnosticoLancaDomainException() {
        Long ordemId = OrdemServicoEmDiagnostico.salvarEm(ordens);
        Long servicoId = servicoNaOrdem(ordemId);
        new ConcluirDiagnostico(ordens, Clock.systemUTC()).executar(ordemId);

        assertThatThrownBy(() -> remover.executar(ordemId, servicoId)).isInstanceOf(DomainException.class);
        assertThat(ordens.buscarPorId(ordemId).orElseThrow().getServicos()).hasSize(1);
    }
}
