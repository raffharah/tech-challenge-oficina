package br.com.fiap.oficina.application.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.Pagina;
import org.junit.jupiter.api.Test;

class ConsultarClientesTest {

    private final ClienteRepositoryEmMemoria clientes = new ClienteRepositoryEmMemoria();
    private final CadastrarCliente cadastrar = new CadastrarCliente(clientes);
    private final ConsultarClientes consultar = new ConsultarClientes(clientes);

    @Test
    void buscaPorCpfComEMascaraSemMascara() {
        ClienteDTO criado = cadastrar.executar(new CadastrarClienteDTO("Maria", "52998224725"));

        assertThat(consultar.buscarPorCpfCnpj("529.982.247-25")).isEqualTo(criado);
        assertThat(consultar.buscarPorCpfCnpj("52998224725")).isEqualTo(criado);
    }

    @Test
    void buscaCnpjAlfanumericoComMinusculas() {
        ClienteDTO criado = cadastrar.executar(new CadastrarClienteDTO("Oficina", "12ABC34501DE35"));

        assertThat(consultar.buscarPorCpfCnpj("12.abc.345/01de-35")).isEqualTo(criado);
    }

    @Test
    void cpfCnpjNaoCadastradoGeraNaoEncontrado() {
        assertThatThrownBy(() -> consultar.buscarPorCpfCnpj("52998224725"))
                .isInstanceOf(NaoEncontradoException.class);
    }

    @Test
    void entradaForaDoFormatoGeraErroDeDominio() {
        assertThatThrownBy(() -> consultar.buscarPorCpfCnpj("123")).isInstanceOf(DomainException.class);
    }

    @Test
    void buscaPorIdInexistenteGeraNaoEncontrado() {
        assertThatThrownBy(() -> consultar.buscarPorId(1L)).isInstanceOf(NaoEncontradoException.class);
    }

    @Test
    void buscaPorIdRetornaCliente() {
        ClienteDTO criado = cadastrar.executar(new CadastrarClienteDTO("Maria", "52998224725"));

        assertThat(consultar.buscarPorId(criado.id())).isEqualTo(criado);
    }

    @Test
    void listagemPaginada() {
        cadastrar.executar(new CadastrarClienteDTO("Maria", "52998224725"));
        cadastrar.executar(new CadastrarClienteDTO("Oficina", "11222333000181"));
        cadastrar.executar(new CadastrarClienteDTO("Outra", "12ABC34501DE35"));

        Pagina<ClienteDTO> pagina = consultar.listar(1, 2);

        assertThat(pagina.itens()).extracting(ClienteDTO::nome).containsExactly("Outra");
        assertThat(pagina.total()).isEqualTo(3);
        assertThat(pagina.pagina()).isEqualTo(1);
        assertThat(pagina.tamanho()).isEqualTo(2);
    }
}
