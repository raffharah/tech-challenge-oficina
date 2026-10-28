package br.com.fiap.oficina.domain.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CpfCnpjTest {

    @Test
    void cpfValidoComMascaraENormalizado() {
        assertThat(new CpfCnpj("529.982.247-25").valor()).isEqualTo("52998224725");
    }

    @Test
    void cpfValidoSemMascara() {
        assertThat(new CpfCnpj("52998224725").valor()).isEqualTo("52998224725");
    }

    @Test
    void cnpjNumericoValidoComMascara() {
        assertThat(new CpfCnpj("11.222.333/0001-81").valor()).isEqualTo("11222333000181");
    }

    @Test
    void cnpjAlfanumericoValidoNormalizadoParaMaiusculas() {
        assertThat(new CpfCnpj("12.abc.345/01de-35").valor()).isEqualTo("12ABC34501DE35");
    }

    @ParameterizedTest
    @ValueSource(strings = {"52998224724", "11222333000182", "12ABC34501DE36"})
    void digitoVerificadorErradoRejeitado(String entrada) {
        assertThatThrownBy(() -> new CpfCnpj(entrada)).isInstanceOf(DomainException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"11111111111", "00000000000000"})
    void todosDigitosIguaisRejeitados(String entrada) {
        assertThatThrownBy(() -> new CpfCnpj(entrada)).isInstanceOf(DomainException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "123", "5299822472", "529982247251", "1122233300018", "11222333000181X", "ABCDEFGHIJKL11"})
    void tamanhoOuFormatoInvalidoRejeitado(String entrada) {
        assertThatThrownBy(() -> new CpfCnpj(entrada)).isInstanceOf(DomainException.class);
    }

    @Test
    void nuloRejeitado() {
        assertThatThrownBy(() -> new CpfCnpj(null)).isInstanceOf(DomainException.class);
    }
}
