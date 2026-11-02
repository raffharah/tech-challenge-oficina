package br.com.fiap.oficina.application.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import br.com.fiap.oficina.domain.ordemservico.VeiculoIdentificado;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class IniciarDiagnosticoTest {

    private static final Instant AGORA = Instant.parse("2026-01-10T10:30:00Z");

    private final OrdemServicoRepositoryEmMemoria ordens = new OrdemServicoRepositoryEmMemoria();
    private final IniciarDiagnostico iniciar = new IniciarDiagnostico(ordens, Clock.fixed(AGORA, ZoneOffset.UTC));

    @Test
    void iniciaDiagnosticoRegistrandoDataDoClock() {
        Long ordemId = ordens.salvar(OrdemServico.abrir(new VeiculoIdentificado(7L, "52998224725", "ABC1D23"),
                Instant.parse("2026-01-10T10:00:00Z"))).getId();

        OrdemServicoDTO dto = iniciar.executar(ordemId);

        assertThat(dto.status()).isEqualTo(StatusOrdemServico.EM_DIAGNOSTICO);
        assertThat(dto.historico().get(1).dataHora()).isEqualTo(AGORA);
        assertThat(ordens.buscarPorId(ordemId).orElseThrow().getStatus())
                .isEqualTo(StatusOrdemServico.EM_DIAGNOSTICO);
    }

    @Test
    void ordemJaEmDiagnosticoLancaDomainException() {
        Long ordemId = OrdemServicoEmDiagnostico.salvarEm(ordens);

        assertThatThrownBy(() -> iniciar.executar(ordemId)).isInstanceOf(DomainException.class);
    }

    @Test
    void ordemInexistenteLancaNaoEncontrado() {
        assertThatThrownBy(() -> iniciar.executar(99L)).isInstanceOf(NaoEncontradoException.class);
    }
}
