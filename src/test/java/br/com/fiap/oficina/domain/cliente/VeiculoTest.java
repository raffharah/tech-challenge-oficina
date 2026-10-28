package br.com.fiap.oficina.domain.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import org.junit.jupiter.api.Test;

class VeiculoTest {

    private static final int ANO_CORRENTE = 2026;
    private static final Placa PLACA = new Placa("ABC1234");

    private Veiculo veiculo(int ano) {
        return new Veiculo(null, PLACA, "Fiat", "Uno", ano, 7L, ANO_CORRENTE);
    }

    @Test
    void anosLimiteAceitos() {
        assertThat(veiculo(1900).getAno()).isEqualTo(1900);
        assertThat(veiculo(ANO_CORRENTE + 1).getAno()).isEqualTo(ANO_CORRENTE + 1);
    }

    @Test
    void anosForaDoLimiteRejeitados() {
        assertThatThrownBy(() -> veiculo(1899)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> veiculo(ANO_CORRENTE + 2)).isInstanceOf(DomainException.class);
    }

    @Test
    void marcaOuModeloVaziosRejeitados() {
        assertThatThrownBy(() -> new Veiculo(null, PLACA, " ", "Uno", 2020, 7L, ANO_CORRENTE))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> new Veiculo(null, PLACA, "Fiat", null, 2020, 7L, ANO_CORRENTE))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void atualizarMantemPlacaECliente() {
        Veiculo veiculo = veiculo(2010);

        veiculo.atualizar("VW", "Gol", 2015, ANO_CORRENTE);

        assertThat(veiculo.getMarca()).isEqualTo("VW");
        assertThat(veiculo.getModelo()).isEqualTo("Gol");
        assertThat(veiculo.getAno()).isEqualTo(2015);
        assertThat(veiculo.getPlaca()).isEqualTo(PLACA);
        assertThat(veiculo.getClienteId()).isEqualTo(7L);
    }

    @Test
    void atualizarComAnoInvalidoRejeitado() {
        Veiculo veiculo = veiculo(2010);

        assertThatThrownBy(() -> veiculo.atualizar("VW", "Gol", 1899, ANO_CORRENTE))
                .isInstanceOf(DomainException.class);
        assertThat(veiculo.getAno()).isEqualTo(2010);
    }
}
