package br.com.fiap.oficina.domain.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class OrdemServicoTest {

    private static final Instant ABERTURA = Instant.parse("2026-01-10T10:00:00Z");
    private static final Instant CONCLUSAO = Instant.parse("2026-01-10T12:00:00Z");

    private static final Instant APROVACAO = Instant.parse("2026-01-11T09:00:00Z");
    private static final Instant FINALIZACAO = Instant.parse("2026-01-12T16:00:00Z");
    private static final Instant ENTREGA = Instant.parse("2026-01-13T10:00:00Z");

    private final VeiculoIdentificado veiculo = new VeiculoIdentificado(7L, "52998224725", "ABC1D23");
    private final Servico troca = new Servico(1L, "Troca de oleo", new BigDecimal("100.00"));
    private final ItemCotado oleo = new ItemCotado(10L, "OLE-1", "Oleo 5W30", new BigDecimal("35.50"));

    private OrdemServico aberta() {
        return OrdemServico.abrir(veiculo, ABERTURA);
    }

    @Test
    void aberturaTerminaEmDiagnosticoComHistoricoDasDuasTransicoes() {
        OrdemServico ordem = aberta();

        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemServico.EM_DIAGNOSTICO);
        assertThat(ordem.getVeiculoId()).isEqualTo(7L);
        assertThat(ordem.getCpfCnpj()).isEqualTo("52998224725");
        assertThat(ordem.getPlaca()).isEqualTo("ABC1D23");
        assertThat(ordem.getHistorico()).containsExactly(
                new MudancaDeStatus(StatusOrdemServico.RECEBIDA, ABERTURA),
                new MudancaDeStatus(StatusOrdemServico.EM_DIAGNOSTICO, ABERTURA));
    }

    @Test
    void transicaoInvalidaLancaDomainExceptionSemAlterarStatusNemHistorico() {
        OrdemServico ordem = aberta();
        ordem.incluirServico(troca);
        ordem.concluirDiagnostico(CONCLUSAO);

        assertThatThrownBy(() -> ordem.concluirDiagnostico(CONCLUSAO)).isInstanceOf(DomainException.class);

        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO);
        assertThat(ordem.getHistorico()).hasSize(3);
    }

    @Test
    void incluiServicoComValorPadrao() {
        OrdemServico ordem = aberta();

        ordem.incluirServico(troca);

        assertThat(ordem.getServicos())
                .containsExactly(new ServicoDaOrdem(1L, "Troca de oleo", new BigDecimal("100.00")));
    }

    @Test
    void incluiItemComPrecoCotado() {
        OrdemServico ordem = aberta();

        ordem.incluirItem(oleo, 4);

        assertThat(ordem.getItens())
                .containsExactly(new ItemDaOrdem(10L, "OLE-1", "Oleo 5W30", 4, new BigDecimal("35.50")));
    }

    @Test
    void mesmoItemSomaQuantidade() {
        OrdemServico ordem = aberta();

        ordem.incluirItem(oleo, 2);
        ordem.incluirItem(oleo, 3);

        assertThat(ordem.getItens()).hasSize(1);
        assertThat(ordem.getItens().get(0).quantidade()).isEqualTo(5);
    }

    @Test
    void quantidadeNaoPositivaERejeitada() {
        OrdemServico ordem = aberta();

        assertThatThrownBy(() -> ordem.incluirItem(oleo, 0)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> ordem.incluirItem(oleo, -1)).isInstanceOf(DomainException.class);
        assertThat(ordem.getItens()).isEmpty();
    }

    @Test
    void removeServicoEItem() {
        OrdemServico ordem = aberta();
        ordem.incluirServico(troca);
        ordem.incluirItem(oleo, 2);

        ordem.removerServico(1L);
        ordem.removerItem(10L);

        assertThat(ordem.getServicos()).isEmpty();
        assertThat(ordem.getItens()).isEmpty();
    }

    @Test
    void servicoRepetidoLancaDomainExceptionSemDuplicar() {
        OrdemServico ordem = aberta();
        ordem.incluirServico(troca);

        assertThatThrownBy(() -> ordem.incluirServico(troca)).isInstanceOf(DomainException.class);

        assertThat(ordem.getServicos()).hasSize(1);
    }

    @Test
    void comandosForaDeDiagnosticoLancamDomainException() {
        OrdemServico ordem = aberta();
        ordem.incluirServico(troca);
        ordem.incluirItem(oleo, 1);
        ordem.concluirDiagnostico(CONCLUSAO);

        assertThatThrownBy(() -> ordem.incluirServico(new Servico(2L, "Alinhamento", BigDecimal.TEN)))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> ordem.removerServico(1L)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> ordem.incluirItem(oleo, 1)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> ordem.removerItem(10L)).isInstanceOf(DomainException.class);
        assertThat(ordem.getServicos()).hasSize(1);
        assertThat(ordem.getItens()).hasSize(1);
    }

    @Test
    void concluirDiagnosticoCalculaTotalEAguardaAprovacao() {
        OrdemServico ordem = aberta();
        ordem.incluirServico(troca);
        ordem.incluirItem(oleo, 2);

        ordem.concluirDiagnostico(CONCLUSAO);

        assertThat(ordem.getTotal()).isEqualTo(new BigDecimal("171.00"));
        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO);
        assertThat(ordem.getHistorico().get(2))
                .isEqualTo(new MudancaDeStatus(StatusOrdemServico.AGUARDANDO_APROVACAO, CONCLUSAO));
    }

    @Test
    void concluirSemServicoLancaDomainExceptionSemMudarStatus() {
        OrdemServico ordem = aberta();
        ordem.incluirItem(oleo, 2);

        assertThatThrownBy(() -> ordem.concluirDiagnostico(CONCLUSAO)).isInstanceOf(DomainException.class);

        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemServico.EM_DIAGNOSTICO);
        assertThat(ordem.getTotal()).isNull();
    }

    private OrdemServico aguardandoAprovacao() {
        OrdemServico ordem = aberta();
        ordem.incluirServico(troca);
        ordem.incluirItem(oleo, 2);
        ordem.concluirDiagnostico(CONCLUSAO);
        return ordem;
    }

    @Test
    void aprovarVaiParaEmExecucaoRegistrandoHistorico() {
        OrdemServico ordem = aguardandoAprovacao();

        ordem.aprovar(APROVACAO);

        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemServico.EM_EXECUCAO);
        assertThat(ordem.getHistorico().get(3))
                .isEqualTo(new MudancaDeStatus(StatusOrdemServico.EM_EXECUCAO, APROVACAO));
    }

    @Test
    void rejeitarCancelaRegistrandoHistorico() {
        OrdemServico ordem = aguardandoAprovacao();

        ordem.rejeitar(APROVACAO);

        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemServico.CANCELADA);
        assertThat(ordem.getHistorico().get(3))
                .isEqualTo(new MudancaDeStatus(StatusOrdemServico.CANCELADA, APROVACAO));
    }

    @Test
    void aprovarERejeitarForaDeAguardandoAprovacaoLancamDomainException() {
        OrdemServico emDiagnostico = aberta();
        OrdemServico emExecucao = aguardandoAprovacao();
        emExecucao.aprovar(APROVACAO);

        assertThatThrownBy(() -> emDiagnostico.aprovar(APROVACAO)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> emDiagnostico.rejeitar(APROVACAO)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> emExecucao.aprovar(APROVACAO)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> emExecucao.rejeitar(APROVACAO)).isInstanceOf(DomainException.class);
        assertThat(emExecucao.getHistorico()).hasSize(4);
    }

    private OrdemServico emExecucao() {
        OrdemServico ordem = aguardandoAprovacao();
        ordem.aprovar(APROVACAO);
        return ordem;
    }

    @Test
    void finalizarVaiParaFinalizadaRegistrandoHistorico() {
        OrdemServico ordem = emExecucao();

        ordem.finalizar(FINALIZACAO);

        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemServico.FINALIZADA);
        assertThat(ordem.getHistorico().get(4))
                .isEqualTo(new MudancaDeStatus(StatusOrdemServico.FINALIZADA, FINALIZACAO));
    }

    @Test
    void finalizarForaDeEmExecucaoLancaDomainException() {
        OrdemServico emDiagnostico = aberta();

        assertThatThrownBy(() -> emDiagnostico.finalizar(FINALIZACAO)).isInstanceOf(DomainException.class);
        assertThat(emDiagnostico.getStatus()).isEqualTo(StatusOrdemServico.EM_DIAGNOSTICO);
    }

    @Test
    void entregarVaiParaEntregueRegistrandoHistorico() {
        OrdemServico ordem = emExecucao();
        ordem.finalizar(FINALIZACAO);

        ordem.entregar(ENTREGA);

        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemServico.ENTREGUE);
        assertThat(ordem.getHistorico().get(5))
                .isEqualTo(new MudancaDeStatus(StatusOrdemServico.ENTREGUE, ENTREGA));
    }

    @Test
    void entregarForaDeFinalizadaLancaDomainException() {
        OrdemServico ordem = emExecucao();

        assertThatThrownBy(() -> ordem.entregar(ENTREGA)).isInstanceOf(DomainException.class);
        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemServico.EM_EXECUCAO);
    }

    @Test
    void ordemCanceladaRejeitaInclusaoConclusaoEAprovacao() {
        OrdemServico ordem = aguardandoAprovacao();
        ordem.rejeitar(APROVACAO);

        assertThatThrownBy(() -> ordem.incluirServico(new Servico(2L, "Alinhamento", BigDecimal.TEN)))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> ordem.incluirItem(oleo, 1)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> ordem.concluirDiagnostico(APROVACAO)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> ordem.aprovar(APROVACAO)).isInstanceOf(DomainException.class);
        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemServico.CANCELADA);
    }
}
