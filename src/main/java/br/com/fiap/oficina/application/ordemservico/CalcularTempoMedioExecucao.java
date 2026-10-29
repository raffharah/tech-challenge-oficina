package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;
import java.time.Duration;
import java.util.List;

public class CalcularTempoMedioExecucao {

    private final OrdemServicoRepository ordens;

    public CalcularTempoMedioExecucao(OrdemServicoRepository ordens) {
        this.ordens = ordens;
    }

    public TempoMedioExecucaoDTO executar() {
        // ponytail: carrega todas as OS finalizadas em memoria; agregar no banco se o volume crescer
        List<Duration> tempos = ordens.listarFinalizadas().stream()
                .flatMap(o -> o.tempoDeExecucao().stream()).toList();
        if (tempos.isEmpty()) {
            return new TempoMedioExecucaoDTO(null, 0);
        }
        double media = tempos.stream().mapToLong(Duration::toSeconds).average().orElseThrow() / 60.0;
        return new TempoMedioExecucaoDTO(media, tempos.size());
    }
}
