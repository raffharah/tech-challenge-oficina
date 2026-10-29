package br.com.fiap.oficina.application.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CalcularTempoMedioExecucaoTest {

    private static final Instant APROVACAO = Instant.parse("2026-01-11T09:00:00Z");

    private final OrdemServicoRepositoryEmMemoria ordens = new OrdemServicoRepositoryEmMemoria();
    private final CalcularTempoMedioExecucao calcular = new CalcularTempoMedioExecucao(ordens);

    private void finalizarAposMinutos(long minutos, boolean entregar) {
        Long ordemId = OrdemServicoEmExecucao.salvarEm(ordens);
        var ordem = ordens.buscarPorId(ordemId).orElseThrow();
        ordem.finalizar(APROVACAO.plus(Duration.ofMinutes(minutos)));
        if (entregar) {
            ordem.entregar(APROVACAO.plus(Duration.ofMinutes(minutos + 30)));
        }
        ordens.salvar(ordem);
    }

    @Test
    void mediaDeSessentaECentoEVinteMinutosENoventaComDuasOrdens() {
        finalizarAposMinutos(60, false);
        finalizarAposMinutos(120, true);
        OrdemServicoEmExecucao.salvarEm(ordens);

        TempoMedioExecucaoDTO dto = calcular.executar();

        assertThat(dto.mediaMinutos()).isEqualTo(90.0);
        assertThat(dto.quantidade()).isEqualTo(2);
    }

    @Test
    void semFinalizadasQuantidadeZeroEMediaNula() {
        OrdemServicoEmExecucao.salvarEm(ordens);

        TempoMedioExecucaoDTO dto = calcular.executar();

        assertThat(dto.mediaMinutos()).isNull();
        assertThat(dto.quantidade()).isZero();
    }
}
