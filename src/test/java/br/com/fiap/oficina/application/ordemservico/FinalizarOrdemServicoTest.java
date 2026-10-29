package br.com.fiap.oficina.application.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class FinalizarOrdemServicoTest {

    private static final Instant AGORA = Instant.parse("2026-01-12T16:00:00Z");

    private final OrdemServicoRepositoryEmMemoria ordens = new OrdemServicoRepositoryEmMemoria();
    private final FinalizarOrdemServico finalizar = new FinalizarOrdemServico(ordens,
            Clock.fixed(AGORA, ZoneOffset.UTC));

    @Test
    void finalizaARegistrandoDataDoClock() {
        Long ordemId = OrdemServicoEmExecucao.salvarEm(ordens);

        OrdemServicoDTO dto = finalizar.executar(ordemId);

        assertThat(dto.status()).isEqualTo(StatusOrdemServico.FINALIZADA);
        assertThat(dto.historico().get(4).dataHora()).isEqualTo(AGORA);
        assertThat(ordens.buscarPorId(ordemId).orElseThrow().getStatus()).isEqualTo(StatusOrdemServico.FINALIZADA);
    }

    @Test
    void emDiagnosticoLancaDomainException() {
        Long ordemId = OrdemServicoEmDiagnostico.salvarEm(ordens);

        assertThatThrownBy(() -> finalizar.executar(ordemId)).isInstanceOf(DomainException.class);
    }

    @Test
    void ordemInexistenteLancaNaoEncontrado() {
        assertThatThrownBy(() -> finalizar.executar(99L)).isInstanceOf(NaoEncontradoException.class);
    }
}
