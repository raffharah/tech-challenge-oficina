package br.com.fiap.oficina.application.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.ordemservico.ItemCotado;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class ConcluirDiagnosticoTest {

    private static final Instant AGORA = Instant.parse("2026-01-10T12:00:00Z");

    private final OrdemServicoRepositoryEmMemoria ordens = new OrdemServicoRepositoryEmMemoria();
    private final ServicoRepositoryEmMemoria servicos = new ServicoRepositoryEmMemoria();
    private final ConcluirDiagnostico concluir = new ConcluirDiagnostico(ordens, Clock.fixed(AGORA, ZoneOffset.UTC));

    @Test
    void devolveTotalEAguardaAprovacao() {
        Long ordemId = OrdemServicoEmDiagnostico.salvarEm(ordens);
        Long servicoId = new CadastrarServico(servicos)
                .executar(new CadastrarServicoDTO("Troca de oleo", new BigDecimal("100.00"))).id();
        new IncluirServicoNaOrdem(ordens, servicos).executar(ordemId, servicoId);
        new IncluirItemNaOrdem(ordens, id -> new ItemCotado(id, "OLE-1", "Oleo", new BigDecimal("35.50")))
                .executar(ordemId, 10L, 2);

        OrdemServicoDTO dto = concluir.executar(ordemId);

        assertThat(dto.total()).isEqualTo(new BigDecimal("171.00"));
        assertThat(dto.status()).isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO);
        assertThat(dto.historico().get(2).dataHora()).isEqualTo(AGORA);
        assertThat(ordens.buscarPorId(ordemId).orElseThrow().getStatus())
                .isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO);
    }

    @Test
    void semServicoLancaDomainExceptionSemMudarStatus() {
        Long ordemId = OrdemServicoEmDiagnostico.salvarEm(ordens);

        assertThatThrownBy(() -> concluir.executar(ordemId)).isInstanceOf(DomainException.class);
        assertThat(ordens.buscarPorId(ordemId).orElseThrow().getStatus())
                .isEqualTo(StatusOrdemServico.EM_DIAGNOSTICO);
    }

    @Test
    void ordemInexistenteLancaNaoEncontrado() {
        assertThatThrownBy(() -> concluir.executar(99L)).isInstanceOf(NaoEncontradoException.class);
    }
}
