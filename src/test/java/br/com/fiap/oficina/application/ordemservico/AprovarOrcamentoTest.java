package br.com.fiap.oficina.application.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

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

    private static final String TITULAR = "529.982.247-25";

    private final OrdemServicoRepositoryEmMemoria ordens = new OrdemServicoRepositoryEmMemoria();
    private final Clock clock = Clock.fixed(AGORA, ZoneOffset.UTC);

    private AprovarOrcamento aprovar(EstoqueDeItensEmMemoria estoque) {
        return new AprovarOrcamento(ordens, new CadastroDeVeiculosSoNormaliza(), estoque, clock);
    }

    @Test
    void comSaldoBaixaOsItensEVaiParaEmExecucao() {
        EstoqueDeItensEmMemoria estoque = new EstoqueDeItensEmMemoria(CATALOGO);
        Long ordemId = OrdemServicoAguardandoAprovacao.salvarEm(ordens);

        OrdemServicoDTO dto = aprovar(estoque).executar(ordemId, TITULAR);

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

        assertThatThrownBy(() -> aprovar(estoque).executar(ordemId, TITULAR))
                .isInstanceOf(DomainException.class);

        assertThat(ordens.buscarPorId(ordemId).orElseThrow().getStatus())
                .isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO);
    }

    @Test
    void foraDeAguardandoAprovacaoLancaDomainExceptionSemChamarEstoque() {
        EstoqueDeItensEmMemoria estoque = new EstoqueDeItensEmMemoria(CATALOGO);
        Long ordemId = OrdemServicoEmDiagnostico.salvarEm(ordens);

        assertThatThrownBy(() -> aprovar(estoque).executar(ordemId, TITULAR))
                .isInstanceOf(DomainException.class);

        assertThat(estoque.baixas).isEmpty();
    }

    @Test
    void ordemInexistenteLancaNaoEncontrado() {
        assertThatThrownBy(() -> aprovar(new EstoqueDeItensEmMemoria(CATALOGO)).executar(99L, TITULAR))
                .isInstanceOf(NaoEncontradoException.class);
    }

    @Test
    void outroTitularLancaNaoEncontradoComMesmaMensagemSemBaixarNemAprovar() {
        EstoqueDeItensEmMemoria estoque = new EstoqueDeItensEmMemoria(CATALOGO);
        Long ordemId = OrdemServicoAguardandoAprovacao.salvarEm(ordens);

        Throwable outroTitular = catchThrowable(() -> aprovar(estoque).executar(ordemId, "11144477735"));
        Throwable inexistente = catchThrowable(() -> aprovar(estoque).executar(99L, TITULAR));

        assertThat(outroTitular).isInstanceOf(NaoEncontradoException.class).hasMessage(inexistente.getMessage());
        assertThat(estoque.baixas).isEmpty();
        assertThat(ordens.buscarPorId(ordemId).orElseThrow().getStatus())
                .isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO);
    }
}
