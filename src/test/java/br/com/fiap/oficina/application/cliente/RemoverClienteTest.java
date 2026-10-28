package br.com.fiap.oficina.application.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.ConflitoException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class RemoverClienteTest {

    private final ClienteRepositoryEmMemoria clientes = new ClienteRepositoryEmMemoria();
    private final VeiculoRepositoryEmMemoria veiculos = new VeiculoRepositoryEmMemoria();
    private final RemoverCliente remover = new RemoverCliente(clientes, veiculos);
    private final ClienteDTO cliente = new CadastrarCliente(clientes)
            .executar(new CadastrarClienteDTO("Maria", "52998224725"));

    @Test
    void semVeiculosRemove() {
        remover.executar(cliente.id());

        assertThat(clientes.quantidade()).isZero();
    }

    @Test
    void comVeiculoGeraConflitoSemRemover() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneOffset.UTC);
        new CadastrarVeiculo(veiculos, clientes, clock)
                .executar(new CadastrarVeiculoDTO("ABC1234", "Fiat", "Uno", 2010, cliente.id()));

        assertThatThrownBy(() -> remover.executar(cliente.id())).isInstanceOf(ConflitoException.class);
        assertThat(clientes.quantidade()).isEqualTo(1);
    }

    @Test
    void inexistenteGeraNaoEncontrado() {
        assertThatThrownBy(() -> remover.executar(99L)).isInstanceOf(NaoEncontradoException.class);
    }
}
