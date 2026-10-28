package br.com.fiap.oficina.application.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import org.junit.jupiter.api.Test;

class AtualizarClienteTest {

    private final ClienteRepositoryEmMemoria clientes = new ClienteRepositoryEmMemoria();
    private final AtualizarCliente atualizar = new AtualizarCliente(clientes);

    @Test
    void alteraNomeMantendoCpfCnpj() {
        ClienteDTO criado = new CadastrarCliente(clientes).executar(new CadastrarClienteDTO("Maria", "52998224725"));

        ClienteDTO dto = atualizar.executar(new AtualizarClienteDTO(criado.id(), "Maria Silva"));

        assertThat(dto.nome()).isEqualTo("Maria Silva");
        assertThat(dto.cpfCnpj()).isEqualTo("52998224725");
        assertThat(clientes.buscarPorId(criado.id()).orElseThrow().getNome()).isEqualTo("Maria Silva");
    }

    @Test
    void idInexistenteGeraNaoEncontrado() {
        assertThatThrownBy(() -> atualizar.executar(new AtualizarClienteDTO(99L, "X")))
                .isInstanceOf(NaoEncontradoException.class);
    }
}
