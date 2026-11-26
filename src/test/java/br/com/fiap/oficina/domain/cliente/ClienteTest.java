package br.com.fiap.oficina.domain.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import org.junit.jupiter.api.Test;

class ClienteTest {

    private static final CpfCnpj CPF = new CpfCnpj("52998224725");

    @Test
    void nomeVazioOuNuloRejeitado() {
        assertThatThrownBy(() -> new Cliente(null, " ", CPF, null)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> new Cliente(null, null, CPF, null)).isInstanceOf(DomainException.class);
    }

    @Test
    void clienteNovoNasceSemEmailERegistrarSubstitui() {
        Cliente cliente = new Cliente(1L, "Maria", CPF, null);
        assertThat(cliente.getEmail()).isNull();

        cliente.registrarEmail(new Email("a@x.com"));
        cliente.registrarEmail(new Email("b@x.com"));

        assertThat(cliente.getEmail().valor()).isEqualTo("b@x.com");
    }

    @Test
    void renomearMantemCpfCnpj() {
        Cliente cliente = new Cliente(1L, "Maria", CPF, null);

        cliente.renomear("Maria Silva");

        assertThat(cliente.getNome()).isEqualTo("Maria Silva");
        assertThat(cliente.getCpfCnpj()).isEqualTo(CPF);
    }

    @Test
    void renomearComNomeVazioRejeitadoMantendoNome() {
        Cliente cliente = new Cliente(1L, "Maria", CPF, null);

        assertThatThrownBy(() -> cliente.renomear("")).isInstanceOf(DomainException.class);
        assertThat(cliente.getNome()).isEqualTo("Maria");
    }
}
