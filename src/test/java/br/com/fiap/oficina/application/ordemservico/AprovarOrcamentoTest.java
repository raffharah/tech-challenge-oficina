package br.com.fiap.oficina.application.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.ordemservico.ItemCotado;
import br.com.fiap.oficina.domain.ordemservico.ItemDaOrdem;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AprovarOrcamentoTest {

    private static final Instant AGORA = Instant.parse("2026-01-11T09:00:00Z");
    private static final Map<Long, ItemCotado> CATALOGO = Map.of(
            10L, new ItemCotado(10L, "OLE-1", "Oleo", new BigDecimal("35.50")));

    private final OrdemServicoRepositoryEmMemoria ordens = new OrdemServicoRepositoryEmMemoria();
    private final Clock clock = Clock.fixed(AGORA, ZoneOffset.UTC);

    @Test
    void comSaldoBaixaOsItensEVaiParaEmExecucao() {
        EstoqueDeItensEmMemoria estoque = new EstoqueDeItensEmMemoria(CATALOGO);
        Long ordemId = OrdemServicoAguardandoAprovacao.salvarEm(ordens);

        OrdemServicoDTO dto = new AprovarOrcamento(ordens, estoque, clock).executar(ordemId);

        assertThat(dto.status()).isEqualTo(StatusOrdemServico.EM_EXECUCAO);
        assertThat(dto.historico().get(3).dataHora()).isEqualTo(AGORA);
        assertThat(estoque.baixas).containsExactly(
                List.of(new ItemDaOrdem(10L, "OLE-1", "Oleo", 2, new BigDecimal("35.50"))));
        assertThat(ordens.buscarPorId(ordemId).orElseThrow().getStatus())
                .isEqualTo(StatusOrdemServico.EM_EXECUCAO);
    }

    @Test
    void faltaDeSaldoPropagaEMantemAguardandoAprovacao() {
        EstoqueDeItensEmMemoria estoque = new EstoqueDeItensEmMemoria(CATALOGO, true);
        Long ordemId = OrdemServicoAguardandoAprovacao.salvarEm(ordens);

        assertThatThrownBy(() -> new AprovarOrcamento(ordens, estoque, clock).executar(ordemId))
                .isInstanceOf(DomainException.class);

        assertThat(ordens.buscarPorId(ordemId).orElseThrow().getStatus())
                .isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO);
    }

    @Test
    void foraDeAguardandoAprovacaoLancaDomainExceptionSemChamarEstoque() {
        EstoqueDeItensEmMemoria estoque = new EstoqueDeItensEmMemoria(CATALOGO);
        Long ordemId = OrdemServicoEmDiagnostico.salvarEm(ordens);

        assertThatThrownBy(() -> new AprovarOrcamento(ordens, estoque, clock).executar(ordemId))
                .isInstanceOf(DomainException.class);

        assertThat(estoque.baixas).isEmpty();
    }

    @Test
    void ordemInexistenteLancaNaoEncontrado() {
        assertThatThrownBy(() -> new AprovarOrcamento(ordens, new EstoqueDeItensEmMemoria(CATALOGO), clock)
                .executar(99L)).isInstanceOf(NaoEncontradoException.class);
    }
}
