package br.com.fiap.oficina.domain.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

    @Test
    void normalizaComTrimEMinusculas() {
        assertThat(new Email(" Ana@Oficina.com ").valor()).isEqualTo("ana@oficina.com");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "ana", "ana@", "ana@oficina", "@oficina.com", "a b@oficina.com"})
    void formatoInvalidoRejeitado(String entrada) {
        assertThatThrownBy(() -> new Email(entrada))
                .isInstanceOfSatisfying(DomainException.class, e -> assertThat(e.getCodigo()).isEqualTo("EMAIL_INVALIDO"));
    }
}
