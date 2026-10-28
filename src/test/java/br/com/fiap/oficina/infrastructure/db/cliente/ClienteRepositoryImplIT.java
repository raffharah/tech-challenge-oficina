package br.com.fiap.oficina.infrastructure.db.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.Pagina;
import br.com.fiap.oficina.domain.cliente.Cliente;
import br.com.fiap.oficina.domain.cliente.ClienteRepository;
import br.com.fiap.oficina.domain.cliente.CpfCnpj;
import br.com.fiap.oficina.infrastructure.TestcontainersConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Import(TestcontainersConfig.class)
class ClienteRepositoryImplIT {

    private static final String CPF_A = "52998224725";
    private static final String CPF_B = "11144477735";

    @Autowired
    ClienteRepository clientes;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void limparBanco() {
        jdbc.execute("truncate table veiculo, cliente restart identity");
    }


    @Test
    void salvaEBuscaPorIdECpfCnpj() {
        String cnpj = "12ABC34501DE35";
        Cliente salvo = clientes.salvar(new Cliente(null, "Oficina", new CpfCnpj(cnpj)));

        assertThat(salvo.getId()).isNotNull();
        assertThat(clientes.buscarPorId(salvo.getId()).orElseThrow().getNome()).isEqualTo("Oficina");
        assertThat(clientes.buscarPorCpfCnpj(new CpfCnpj("12.abc.345/01de-35")).orElseThrow().getId())
                .isEqualTo(salvo.getId());
    }

    @Test
    void atualizaNomeMantendoId() {
        Cliente salvo = clientes.salvar(new Cliente(null, "Maria", new CpfCnpj(CPF_A)));
        salvo.renomear("Maria Silva");

        Cliente atualizado = clientes.salvar(salvo);

        assertThat(atualizado.getId()).isEqualTo(salvo.getId());
        assertThat(clientes.buscarPorId(salvo.getId()).orElseThrow().getNome()).isEqualTo("Maria Silva");
    }

    @Test
    void listaPaginado() {
        clientes.salvar(new Cliente(null, "Paulo", new CpfCnpj(CPF_B)));

        Pagina<Cliente> pagina = clientes.listar(0, 1);

        assertThat(pagina.itens()).hasSize(1);
        assertThat(pagina.total()).isGreaterThanOrEqualTo(1);
        assertThat(pagina.tamanho()).isEqualTo(1);
    }

    @Test
    void removeCliente() {
        String cpf = "39053344705";
        Cliente salvo = clientes.salvar(new Cliente(null, "Remover", new CpfCnpj(cpf)));

        clientes.remover(salvo.getId());

        assertThat(clientes.buscarPorId(salvo.getId())).isEmpty();
    }

    @Test
    void cpfCnpjDuplicadoViolaConstraint() {
        String cpf = "86288366757";
        clientes.salvar(new Cliente(null, "Um", new CpfCnpj(cpf)));

        assertThatThrownBy(() -> clientes.salvar(new Cliente(null, "Dois", new CpfCnpj(cpf))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
