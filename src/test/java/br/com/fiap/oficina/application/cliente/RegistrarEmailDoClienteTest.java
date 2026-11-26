package br.com.fiap.oficina.application.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.cliente.CpfCnpj;
import org.junit.jupiter.api.Test;

class RegistrarEmailDoClienteTest {

    private static final String CPF = "52998224725";

    private final ClienteRepositoryEmMemoria clientes = new ClienteRepositoryEmMemoria();
    private final RegistrarEmailDoCliente registrar = new RegistrarEmailDoCliente(clientes);

    @Test
    void gravaESubstituiEmail() {
        new CadastrarCliente(clientes).executar(new CadastrarClienteDTO("Maria", CPF));

        registrar.executar(CPF, "Maria@Oficina.com");
        ClienteDTO dto = registrar.executar("529.982.247-25", "nova@oficina.com");

        assertThat(dto.email()).isEqualTo("nova@oficina.com");
        assertThat(clientes.buscarPorCpfCnpj(new CpfCnpj(CPF)).orElseThrow().getEmail().valor())
                .isEqualTo("nova@oficina.com");
    }

    @Test
    void cpfCnpjSemCadastroGeraNaoEncontrado() {
        assertThatThrownBy(() -> registrar.executar(CPF, "maria@oficina.com"))
                .isInstanceOf(NaoEncontradoException.class);
    }

    @Test
    void emailInvalidoNaoSalva() {
        new CadastrarCliente(clientes).executar(new CadastrarClienteDTO("Maria", CPF));
        registrar.executar(CPF, "maria@oficina.com");

        assertThatThrownBy(() -> registrar.executar(CPF, "invalido"))
                .isInstanceOfSatisfying(DomainException.class, e -> assertThat(e.getCodigo()).isEqualTo("EMAIL_INVALIDO"));
        assertThat(clientes.buscarPorCpfCnpj(new CpfCnpj(CPF)).orElseThrow().getEmail().valor())
                .isEqualTo("maria@oficina.com");
    }
}
