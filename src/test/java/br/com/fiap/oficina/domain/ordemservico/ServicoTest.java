package br.com.fiap.oficina.domain.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ServicoTest {

    @Test
    void criaServicoComNomeEValorPadrao() {
        Servico servico = new Servico(null, " Troca de oleo ", new BigDecimal("120.00"));

        assertThat(servico.getNome()).isEqualTo("Troca de oleo");
        assertThat(servico.getValorPadrao()).isEqualByComparingTo("120.00");
    }

    @Test
    void valorPadraoZeroOuNegativoRejeitado() {
        assertThatThrownBy(() -> new Servico(null, "Alinhamento", BigDecimal.ZERO))
                .isInstanceOf(DomainException.class)
                .extracting("codigo").isEqualTo("VALOR_PADRAO_INVALIDO");
        assertThatThrownBy(() -> new Servico(null, "Alinhamento", new BigDecimal("-0.01")))
                .isInstanceOf(DomainException.class)
                .extracting("codigo").isEqualTo("VALOR_PADRAO_INVALIDO");
        assertThatThrownBy(() -> new Servico(null, "Alinhamento", null))
                .isInstanceOf(DomainException.class)
                .extracting("codigo").isEqualTo("VALOR_PADRAO_INVALIDO");
    }

    @Test
    void nomeVazioRejeitado() {
        assertThatThrownBy(() -> new Servico(null, " ", BigDecimal.TEN))
                .isInstanceOf(DomainException.class)
                .extracting("codigo").isEqualTo("NOME_OBRIGATORIO");
        assertThatThrownBy(() -> new Servico(null, null, BigDecimal.TEN))
                .isInstanceOf(DomainException.class)
                .extracting("codigo").isEqualTo("NOME_OBRIGATORIO");
    }

    @Test
    void alterarAplicaNomeEValor() {
        Servico servico = new Servico(1L, "Alinhamento", BigDecimal.TEN);

        servico.alterar("Alinhamento 3D", new BigDecimal("90.50"));

        assertThat(servico.getId()).isEqualTo(1L);
        assertThat(servico.getNome()).isEqualTo("Alinhamento 3D");
        assertThat(servico.getValorPadrao()).isEqualByComparingTo("90.50");
    }

    @Test
    void alterarComDadosInvalidosRejeitadoMantendoEstado() {
        Servico servico = new Servico(1L, "Alinhamento", BigDecimal.TEN);

        assertThatThrownBy(() -> servico.alterar("Outro", BigDecimal.ZERO))
                .isInstanceOf(DomainException.class)
                .extracting("codigo").isEqualTo("VALOR_PADRAO_INVALIDO");
        assertThatThrownBy(() -> servico.alterar("", BigDecimal.ONE))
                .isInstanceOf(DomainException.class)
                .extracting("codigo").isEqualTo("NOME_OBRIGATORIO");
        assertThat(servico.getNome()).isEqualTo("Alinhamento");
        assertThat(servico.getValorPadrao()).isEqualByComparingTo("10");
    }
}
