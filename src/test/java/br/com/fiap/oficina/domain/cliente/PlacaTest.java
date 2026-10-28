package br.com.fiap.oficina.domain.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PlacaTest {

    @Test
    void padraoAntigoNormalizadoSemHifenEmMaiuscula() {
        assertThat(new Placa("abc-1234").valor()).isEqualTo("ABC1234");
    }

    @Test
    void padraoMercosulValido() {
        assertThat(new Placa("ABC1D23").valor()).isEqualTo("ABC1D23");
    }

    @ParameterizedTest
    @ValueSource(strings = {"AB12345", "ABCD123", "", "   ", "ABC12D3", "ABC123"})
    void formatoInvalidoRejeitado(String entrada) {
        assertThatThrownBy(() -> new Placa(entrada)).isInstanceOf(DomainException.class);
    }

    @Test
    void nulaRejeitada() {
        assertThatThrownBy(() -> new Placa(null)).isInstanceOf(DomainException.class);
    }
}
