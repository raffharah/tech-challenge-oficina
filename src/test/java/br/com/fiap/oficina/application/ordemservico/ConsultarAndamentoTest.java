package br.com.fiap.oficina.application.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.ordemservico.CadastroDeVeiculos;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import br.com.fiap.oficina.domain.ordemservico.VeiculoIdentificado;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ConsultarAndamentoTest {

    private final OrdemServicoRepositoryEmMemoria ordens = new OrdemServicoRepositoryEmMemoria();
    private final CadastroDeVeiculos cadastro = new CadastroDeVeiculos() {
        @Override
        public VeiculoIdentificado identificar(String cpfCnpj, String placa) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String normalizarCpfCnpj(String cpfCnpj) {
            return cpfCnpj.replaceAll("\\D", "");
        }
    };
    private final ConsultarAndamento consultar = new ConsultarAndamento(ordens, cadastro);

    @Test
    void titularRecebeStatusHistoricoComponentesETotal() {
        Long ordemId = OrdemServicoAguardandoAprovacao.salvarEm(ordens);

        OrdemServicoDTO dto = consultar.executar(ordemId, "529.982.247-25");

        assertThat(dto.status()).isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO);
        assertThat(dto.historico()).extracting(OrdemServicoDTO.MudancaDeStatusDTO::status).containsExactly(
                StatusOrdemServico.RECEBIDA, StatusOrdemServico.EM_DIAGNOSTICO,
                StatusOrdemServico.AGUARDANDO_APROVACAO);
        assertThat(dto.servicos()).hasSize(1);
        assertThat(dto.itens()).hasSize(1);
        assertThat(dto.total()).isEqualByComparingTo(new BigDecimal("171.00"));
    }

    @Test
    void outroTitularEOrdemInexistenteLancamNaoEncontradoComMesmaMensagem() {
        Long ordemId = OrdemServicoAguardandoAprovacao.salvarEm(ordens);

        Throwable outroTitular = org.assertj.core.api.Assertions
                .catchThrowable(() -> consultar.executar(ordemId, "11144477735"));
        Throwable inexistente = org.assertj.core.api.Assertions
                .catchThrowable(() -> consultar.executar(99L, "52998224725"));

        assertThat(outroTitular).isInstanceOf(NaoEncontradoException.class);
        assertThat(inexistente).isInstanceOf(NaoEncontradoException.class).hasMessage(outroTitular.getMessage());
        assertThatThrownBy(() -> consultar.executar(ordemId, "11144477735"))
                .extracting("codigo").isEqualTo("ORDEM_SERVICO_NAO_ENCONTRADA");
    }
}
