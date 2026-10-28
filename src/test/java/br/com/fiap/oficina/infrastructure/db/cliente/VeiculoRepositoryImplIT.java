package br.com.fiap.oficina.infrastructure.db.cliente;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.fiap.oficina.domain.cliente.Cliente;
import br.com.fiap.oficina.domain.cliente.ClienteRepository;
import br.com.fiap.oficina.domain.cliente.CpfCnpj;
import br.com.fiap.oficina.domain.cliente.Placa;
import br.com.fiap.oficina.domain.cliente.Veiculo;
import br.com.fiap.oficina.domain.cliente.VeiculoRepository;
import br.com.fiap.oficina.infrastructure.TestcontainersConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Import(TestcontainersConfig.class)
class VeiculoRepositoryImplIT {

    @Autowired
    ClienteRepository clientes;

    @Autowired
    VeiculoRepository veiculos;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void limparBanco() {
        jdbc.execute("truncate table veiculo, cliente restart identity");
    }

    private Cliente cliente(String cpf) {
        return clientes.salvar(new Cliente(null, "Dono", new CpfCnpj(cpf)));
    }

    private Veiculo veiculo(String placa, Long clienteId) {
        return new Veiculo(null, new Placa(placa), "Fiat", "Uno", 2010, clienteId, 2026);
    }

    @Test
    void salvaEBuscaPorPlacaEId() {
        Cliente dono = cliente("52998224725");
        Veiculo salvo = veiculos.salvar(veiculo("AAA1111", dono.getId()));

        Veiculo porPlaca = veiculos.buscarPorPlaca(new Placa("aaa-1111")).orElseThrow();

        assertThat(porPlaca.getId()).isEqualTo(salvo.getId());
        assertThat(porPlaca.getMarca()).isEqualTo("Fiat");
        assertThat(porPlaca.getAno()).isEqualTo(2010);
        assertThat(porPlaca.getClienteId()).isEqualTo(dono.getId());
        assertThat(veiculos.buscarPorId(salvo.getId())).isPresent();
    }

    @Test
    void listaSoDoClienteEInformaExistencia() {
        Cliente maria = cliente("11144477735");
        Cliente paulo = cliente("39053344705");
        veiculos.salvar(veiculo("BBB1111", maria.getId()));
        veiculos.salvar(veiculo("BBB2222", paulo.getId()));

        assertThat(veiculos.listarPorCliente(maria.getId())).extracting(v -> v.getPlaca().valor())
                .containsExactly("BBB1111");
        assertThat(veiculos.existePorCliente(maria.getId())).isTrue();
        assertThat(veiculos.existePorCliente(Long.MAX_VALUE)).isFalse();
    }

    @Test
    void removeVeiculo() {
        Cliente dono = cliente("86288366757");
        Veiculo salvo = veiculos.salvar(veiculo("CCC1111", dono.getId()));

        veiculos.remover(salvo.getId());

        assertThat(veiculos.buscarPorId(salvo.getId())).isEmpty();
        assertThat(veiculos.existePorCliente(dono.getId())).isFalse();
    }
}
