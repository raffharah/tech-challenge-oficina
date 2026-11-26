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

    @Test
    void tamanhoNoLimiteAceito() {
        String local = "a".repeat(64);
        String dominio = "b".repeat(254 - 64 - 1 - 4) + ".com";

        assertThat(new Email(local + "@" + dominio).valor()).hasSize(254);
    }

    @Test
    void acimaDe254CaracteresRejeitado() {
        String email = "a".repeat(64) + "@" + "b".repeat(254 - 64 - 4) + ".com";

        assertThatThrownBy(() -> new Email(email))
                .isInstanceOfSatisfying(DomainException.class, e -> assertThat(e.getCodigo()).isEqualTo("EMAIL_INVALIDO"));
    }

    @Test
    void parteLocalAcimaDe64CaracteresRejeitada() {
        assertThatThrownBy(() -> new Email("a".repeat(65) + "@oficina.com"))
                .isInstanceOfSatisfying(DomainException.class, e -> assertThat(e.getCodigo()).isEqualTo("EMAIL_INVALIDO"));
    }
}
