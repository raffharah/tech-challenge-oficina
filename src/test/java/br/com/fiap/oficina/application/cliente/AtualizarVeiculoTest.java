package br.com.fiap.oficina.application.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class AtualizarVeiculoTest {

    private final ClienteRepositoryEmMemoria clientes = new ClienteRepositoryEmMemoria();
    private final VeiculoRepositoryEmMemoria veiculos = new VeiculoRepositoryEmMemoria();
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneOffset.UTC);
    private final AtualizarVeiculo atualizar = new AtualizarVeiculo(veiculos, clock);

    @Test
    void alteraCamposMantendoPlacaECliente() {
        Long clienteId = new CadastrarCliente(clientes).executar(new CadastrarClienteDTO("Maria", "52998224725")).id();
        VeiculoDTO criado = new CadastrarVeiculo(veiculos, clientes, clock)
                .executar(new CadastrarVeiculoDTO("ABC1234", "Fiat", "Uno", 2010, clienteId));

        VeiculoDTO dto = atualizar.executar(new AtualizarVeiculoDTO(criado.id(), "VW", "Gol", 2015));

        assertThat(dto.marca()).isEqualTo("VW");
        assertThat(dto.modelo()).isEqualTo("Gol");
        assertThat(dto.ano()).isEqualTo(2015);
        assertThat(dto.placa()).isEqualTo("ABC1234");
        assertThat(dto.clienteId()).isEqualTo(clienteId);
    }

    @Test
    void inexistenteGeraNaoEncontrado() {
        assertThatThrownBy(() -> atualizar.executar(new AtualizarVeiculoDTO(99L, "VW", "Gol", 2015)))
                .isInstanceOf(NaoEncontradoException.class);
    }
}
