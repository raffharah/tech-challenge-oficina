package br.com.fiap.oficina.application.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class DetalharOrdemServicoTest {

    private final OrdemServicoRepositoryEmMemoria ordens = new OrdemServicoRepositoryEmMemoria();
    private final DetalharOrdemServico detalhar = new DetalharOrdemServico(ordens);

    @Test
    void devolveClienteVeiculoComponentesTotalEHistorico() {
        Long ordemId = OrdemServicoAguardandoAprovacao.salvarEm(ordens);

        OrdemServicoDTO dto = detalhar.executar(ordemId);

        assertThat(dto.cpfCnpj()).isEqualTo("52998224725");
        assertThat(dto.placa()).isEqualTo("ABC1D23");
        assertThat(dto.servicos()).hasSize(1);
        assertThat(dto.itens()).hasSize(1);
        assertThat(dto.total()).isEqualByComparingTo(new BigDecimal("171.00"));
        assertThat(dto.historico()).extracting(OrdemServicoDTO.MudancaDeStatusDTO::status).containsExactly(
                StatusOrdemServico.RECEBIDA, StatusOrdemServico.EM_DIAGNOSTICO,
                StatusOrdemServico.AGUARDANDO_APROVACAO);
    }

    @Test
    void inexistenteLancaNaoEncontrado() {
        assertThatThrownBy(() -> detalhar.executar(99L)).isInstanceOf(NaoEncontradoException.class);
    }
}
