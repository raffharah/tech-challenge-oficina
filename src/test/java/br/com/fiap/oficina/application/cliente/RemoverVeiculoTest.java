package br.com.fiap.oficina.application.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class RemoverVeiculoTest {

    private final ClienteRepositoryEmMemoria clientes = new ClienteRepositoryEmMemoria();
    private final VeiculoRepositoryEmMemoria veiculos = new VeiculoRepositoryEmMemoria();
    private final RemoverVeiculo remover = new RemoverVeiculo(veiculos);

    @Test
    void removeVeiculoExistente() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneOffset.UTC);
        Long clienteId = new CadastrarCliente(clientes).executar(new CadastrarClienteDTO("Maria", "52998224725")).id();
        VeiculoDTO criado = new CadastrarVeiculo(veiculos, clientes, clock)
                .executar(new CadastrarVeiculoDTO("ABC1234", "Fiat", "Uno", 2010, clienteId));

        remover.executar(criado.id());

        assertThat(veiculos.quantidade()).isZero();
    }

    @Test
    void inexistenteGeraNaoEncontrado() {
        assertThatThrownBy(() -> remover.executar(99L)).isInstanceOf(NaoEncontradoException.class);
    }
}
