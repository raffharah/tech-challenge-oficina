package br.com.fiap.oficina.application.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class RejeitarOrcamentoTest {

    private static final Instant AGORA = Instant.parse("2026-01-11T09:00:00Z");
    private static final String TITULAR = "529.982.247-25";

    private final OrdemServicoRepositoryEmMemoria ordens = new OrdemServicoRepositoryEmMemoria();
    private final RejeitarOrcamento rejeitar = new RejeitarOrcamento(ordens, new CadastroDeVeiculosSoNormaliza(),
            Clock.fixed(AGORA, ZoneOffset.UTC));

    @Test
    void cancelaAOrdem() {
        Long ordemId = OrdemServicoAguardandoAprovacao.salvarEm(ordens);

        OrdemServicoDTO dto = rejeitar.executar(ordemId, TITULAR);

        assertThat(dto.status()).isEqualTo(StatusOrdemServico.CANCELADA);
        assertThat(dto.historico().get(3).dataHora()).isEqualTo(AGORA);
        assertThat(ordens.buscarPorId(ordemId).orElseThrow().getStatus()).isEqualTo(StatusOrdemServico.CANCELADA);
    }

    @Test
    void foraDeAguardandoAprovacaoLancaDomainException() {
        Long ordemId = OrdemServicoEmDiagnostico.salvarEm(ordens);

        assertThatThrownBy(() -> rejeitar.executar(ordemId, TITULAR)).isInstanceOf(DomainException.class);
    }

    @Test
    void ordemInexistenteLancaNaoEncontrado() {
        assertThatThrownBy(() -> rejeitar.executar(99L, TITULAR)).isInstanceOf(NaoEncontradoException.class);
    }

    @Test
    void outroTitularLancaNaoEncontradoComMesmaMensagemSemCancelar() {
        Long ordemId = OrdemServicoAguardandoAprovacao.salvarEm(ordens);

        Throwable outroTitular = catchThrowable(() -> rejeitar.executar(ordemId, "11144477735"));
        Throwable inexistente = catchThrowable(() -> rejeitar.executar(99L, TITULAR));

        assertThat(outroTitular).isInstanceOf(NaoEncontradoException.class).hasMessage(inexistente.getMessage());
        assertThat(ordens.buscarPorId(ordemId).orElseThrow().getStatus())
                .isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO);
    }
}
