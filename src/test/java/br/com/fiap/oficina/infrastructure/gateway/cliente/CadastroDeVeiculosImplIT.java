package br.com.fiap.oficina.infrastructure.gateway.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.ordemservico.CadastroDeVeiculos;
import br.com.fiap.oficina.domain.ordemservico.VeiculoIdentificado;
import br.com.fiap.oficina.infrastructure.TestcontainersConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Import(TestcontainersConfig.class)
class CadastroDeVeiculosImplIT {

    @Autowired
    CadastroDeVeiculos cadastro;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void prepararBanco() {
        jdbc.execute("truncate table ordem_servico, veiculo, cliente, servico, item_estoque restart identity cascade");
        jdbc.update("insert into cliente (nome, cpf_cnpj) values ('Maria', '52998224725')");
        jdbc.update("insert into cliente (nome, cpf_cnpj) values ('Joao', '11144477735')");
        jdbc.update("insert into veiculo (placa, marca, modelo, ano, cliente_id) values ('ABC1D23', 'Fiat', 'Uno', 2020, 1)");
    }

    @Test
    void identificaComCpfMascaradoEPlacaMinuscula() {
        VeiculoIdentificado veiculo = cadastro.identificar("529.982.247-25", "abc-1d23");

        assertThat(veiculo).isEqualTo(new VeiculoIdentificado(1L, "52998224725", "ABC1D23"));
    }

    @Test
    void cpfOuPlacaInvalidosLancamDomainException() {
        assertThatThrownBy(() -> cadastro.identificar("123", "ABC1D23")).isExactlyInstanceOf(DomainException.class)
                .extracting("codigo").isEqualTo("CPF_CNPJ_INVALIDO");
        assertThatThrownBy(() -> cadastro.identificar("52998224725", "XX")).isExactlyInstanceOf(DomainException.class)
                .extracting("codigo").isEqualTo("PLACA_INVALIDA");
    }

    @Test
    void clienteOuVeiculoInexistenteLancaNaoEncontrado() {
        assertThatThrownBy(() -> cadastro.identificar("39053344705", "ABC1D23"))
                .isInstanceOf(NaoEncontradoException.class);
        assertThatThrownBy(() -> cadastro.identificar("52998224725", "ZZZ9Z99"))
                .isInstanceOf(NaoEncontradoException.class);
    }

    @Test
    void veiculoDeOutroClienteLancaDomainException() {
        assertThatThrownBy(() -> cadastro.identificar("11144477735", "ABC1D23"))
                .isExactlyInstanceOf(DomainException.class).extracting("codigo")
                .isEqualTo("VEICULO_DE_OUTRO_CLIENTE");
    }

    @Test
    void normalizaCpfMascaradoParaSoDigitos() {
        assertThat(cadastro.normalizarCpfCnpj("529.982.247-25")).isEqualTo("52998224725");
    }

    @Test
    void normalizarFormatoInvalidoLancaDomainException() {
        assertThatThrownBy(() -> cadastro.normalizarCpfCnpj("123")).isExactlyInstanceOf(DomainException.class)
                .extracting("codigo").isEqualTo("CPF_CNPJ_INVALIDO");
    }
}
