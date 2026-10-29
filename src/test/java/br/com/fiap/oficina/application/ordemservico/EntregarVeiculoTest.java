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

class EntregarVeiculoTest {

    private static final Instant AGORA = Instant.parse("2026-01-13T10:00:00Z");

    private final OrdemServicoRepositoryEmMemoria ordens = new OrdemServicoRepositoryEmMemoria();
    private final EntregarVeiculo entregar = new EntregarVeiculo(ordens, Clock.fixed(AGORA, ZoneOffset.UTC));

    @Test
    void entregaARegistrandoDataDoClock() {
        Long ordemId = OrdemServicoEmExecucao.salvarEm(ordens);
        var ordem = ordens.buscarPorId(ordemId).orElseThrow();
        ordem.finalizar(Instant.parse("2026-01-12T16:00:00Z"));
        ordens.salvar(ordem);

        OrdemServicoDTO dto = entregar.executar(ordemId);

        assertThat(dto.status()).isEqualTo(StatusOrdemServico.ENTREGUE);
        assertThat(dto.historico().get(5).dataHora()).isEqualTo(AGORA);
        assertThat(ordens.buscarPorId(ordemId).orElseThrow().getStatus()).isEqualTo(StatusOrdemServico.ENTREGUE);
    }

    @Test
    void emExecucaoLancaDomainException() {
        Long ordemId = OrdemServicoEmExecucao.salvarEm(ordens);

        assertThatThrownBy(() -> entregar.executar(ordemId)).isInstanceOf(DomainException.class);
    }

    @Test
    void ordemInexistenteLancaNaoEncontrado() {
        assertThatThrownBy(() -> entregar.executar(99L)).isInstanceOf(NaoEncontradoException.class);
    }
}
