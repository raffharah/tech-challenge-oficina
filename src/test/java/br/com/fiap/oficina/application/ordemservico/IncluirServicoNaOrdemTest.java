package br.com.fiap.oficina.application.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class IncluirServicoNaOrdemTest {

    private final OrdemServicoRepositoryEmMemoria ordens = new OrdemServicoRepositoryEmMemoria();
    private final ServicoRepositoryEmMemoria servicos = new ServicoRepositoryEmMemoria();
    private final IncluirServicoNaOrdem incluir = new IncluirServicoNaOrdem(ordens, servicos);

    private Long servicoCadastrado() {
        return new CadastrarServico(servicos).executar(new CadastrarServicoDTO("Troca de oleo", new BigDecimal("100.00")))
                .id();
    }

    @Test
    void incluiServicoComValorPadrao() {
        Long ordemId = OrdemServicoEmDiagnostico.salvarEm(ordens);
        Long servicoId = servicoCadastrado();

        OrdemServicoDTO dto = incluir.executar(ordemId, servicoId);

        assertThat(dto.servicos()).containsExactly(
                new OrdemServicoDTO.ServicoDaOrdemDTO(servicoId, "Troca de oleo", new BigDecimal("100.00")));
        assertThat(ordens.buscarPorId(ordemId).orElseThrow().getServicos()).hasSize(1);
    }

    @Test
    void servicoInexistenteLancaDomainException() {
        Long ordemId = OrdemServicoEmDiagnostico.salvarEm(ordens);

        assertThatThrownBy(() -> incluir.executar(ordemId, 99L)).isExactlyInstanceOf(DomainException.class);
    }

    @Test
    void ordemInexistenteLancaNaoEncontrado() {
        Long servicoId = servicoCadastrado();

        assertThatThrownBy(() -> incluir.executar(99L, servicoId)).isInstanceOf(NaoEncontradoException.class);
    }

    @Test
    void servicoRepetidoLancaDomainException() {
        Long ordemId = OrdemServicoEmDiagnostico.salvarEm(ordens);
        Long servicoId = servicoCadastrado();
        incluir.executar(ordemId, servicoId);

        assertThatThrownBy(() -> incluir.executar(ordemId, servicoId)).isInstanceOf(DomainException.class);
        assertThat(ordens.buscarPorId(ordemId).orElseThrow().getServicos()).hasSize(1);
    }
}
