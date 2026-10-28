package br.com.fiap.oficina.application.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.ConflitoException;
import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class CadastrarVeiculoTest {

    private final ClienteRepositoryEmMemoria clientes = new ClienteRepositoryEmMemoria();
    private final VeiculoRepositoryEmMemoria veiculos = new VeiculoRepositoryEmMemoria();
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneOffset.UTC);
    private final CadastrarVeiculo cadastrar = new CadastrarVeiculo(veiculos, clientes, clock);
    private final Long clienteId = new CadastrarCliente(clientes)
            .executar(new CadastrarClienteDTO("Maria", "52998224725")).id();

    private CadastrarVeiculoDTO dto(String placa, int ano) {
        return new CadastrarVeiculoDTO(placa, "Fiat", "Uno", ano, clienteId);
    }

    @Test
    void cadastraComPlacaNormalizada() {
        VeiculoDTO dto = cadastrar.executar(dto("abc-1234", 2010));

        assertThat(dto.id()).isNotNull();
        assertThat(dto.placa()).isEqualTo("ABC1234");
        assertThat(dto.clienteId()).isEqualTo(clienteId);
        assertThat(dto.ano()).isEqualTo(2010);
    }

    @Test
    void placaMercosulAceita() {
        assertThat(cadastrar.executar(dto("ABC1D23", 2020)).placa()).isEqualTo("ABC1D23");
    }

    @Test
    void placaInvalidaNaoPersiste() {
        assertThatThrownBy(() -> cadastrar.executar(dto("AB12345", 2010))).isInstanceOf(DomainException.class);
        assertThat(veiculos.quantidade()).isZero();
    }

    @Test
    void placaRepetidaGeraConflito() {
        cadastrar.executar(dto("ABC1234", 2010));

        assertThatThrownBy(() -> cadastrar.executar(dto("abc-1234", 2011))).isInstanceOf(ConflitoException.class);
        assertThat(veiculos.quantidade()).isEqualTo(1);
    }

    @Test
    void anoForaDoLimiteGeraErroDeDominio() {
        assertThatThrownBy(() -> cadastrar.executar(dto("ABC1234", 1899))).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> cadastrar.executar(dto("ABC1234", 2028))).isInstanceOf(DomainException.class);
        assertThat(cadastrar.executar(dto("ABC1234", 2027)).ano()).isEqualTo(2027);
    }

    @Test
    void clienteInexistenteGeraNaoEncontrado() {
        assertThatThrownBy(() -> cadastrar.executar(new CadastrarVeiculoDTO("ABC1234", "Fiat", "Uno", 2010, 99L)))
                .isInstanceOf(NaoEncontradoException.class);
    }
}
