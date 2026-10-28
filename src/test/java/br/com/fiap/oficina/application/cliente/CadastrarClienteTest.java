package br.com.fiap.oficina.application.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.ConflitoException;
import br.com.fiap.oficina.domain.DomainException;
import org.junit.jupiter.api.Test;

class CadastrarClienteTest {

    private final ClienteRepositoryEmMemoria clientes = new ClienteRepositoryEmMemoria();
    private final CadastrarCliente cadastrar = new CadastrarCliente(clientes);

    @Test
    void cadastraClienteComCpfNormalizado() {
        ClienteDTO dto = cadastrar.executar(new CadastrarClienteDTO("Maria", "529.982.247-25"));

        assertThat(dto.id()).isNotNull();
        assertThat(dto.nome()).isEqualTo("Maria");
        assertThat(dto.cpfCnpj()).isEqualTo("52998224725");
        assertThat(clientes.quantidade()).isEqualTo(1);
    }

    @Test
    void cnpjAlfanumericoCadastrado() {
        ClienteDTO dto = cadastrar.executar(new CadastrarClienteDTO("Oficina", "12.ABC.345/01DE-35"));

        assertThat(dto.cpfCnpj()).isEqualTo("12ABC34501DE35");
    }

    @Test
    void cpfCnpjInvalidoNaoPersiste() {
        assertThatThrownBy(() -> cadastrar.executar(new CadastrarClienteDTO("Maria", "111.111.111-11")))
                .isInstanceOf(DomainException.class);
        assertThat(clientes.quantidade()).isZero();
    }

    @Test
    void duplicadoComMascaraDiferenteGeraConflito() {
        cadastrar.executar(new CadastrarClienteDTO("Maria", "52998224725"));

        assertThatThrownBy(() -> cadastrar.executar(new CadastrarClienteDTO("Outra", "529.982.247-25")))
                .isInstanceOf(ConflitoException.class);
        assertThat(clientes.quantidade()).isEqualTo(1);
    }
}
