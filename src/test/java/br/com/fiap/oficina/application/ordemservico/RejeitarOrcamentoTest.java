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

class RejeitarOrcamentoTest {

    private static final Instant AGORA = Instant.parse("2026-01-11T09:00:00Z");

    private final OrdemServicoRepositoryEmMemoria ordens = new OrdemServicoRepositoryEmMemoria();
    private final RejeitarOrcamento rejeitar = new RejeitarOrcamento(ordens, Clock.fixed(AGORA, ZoneOffset.UTC));

    @Test
    void cancelaAOrdem() {
        Long ordemId = OrdemServicoAguardandoAprovacao.salvarEm(ordens);

        OrdemServicoDTO dto = rejeitar.executar(ordemId);

        assertThat(dto.status()).isEqualTo(StatusOrdemServico.CANCELADA);
        assertThat(dto.historico().get(3).dataHora()).isEqualTo(AGORA);
        assertThat(ordens.buscarPorId(ordemId).orElseThrow().getStatus()).isEqualTo(StatusOrdemServico.CANCELADA);
    }

    @Test
    void foraDeAguardandoAprovacaoLancaDomainException() {
        Long ordemId = OrdemServicoEmDiagnostico.salvarEm(ordens);

        assertThatThrownBy(() -> rejeitar.executar(ordemId)).isInstanceOf(DomainException.class);
    }

    @Test
    void ordemInexistenteLancaNaoEncontrado() {
        assertThatThrownBy(() -> rejeitar.executar(99L)).isInstanceOf(NaoEncontradoException.class);
    }
}
