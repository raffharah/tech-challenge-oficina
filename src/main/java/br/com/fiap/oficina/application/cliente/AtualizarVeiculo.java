package br.com.fiap.oficina.application.cliente;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.cliente.Veiculo;
import br.com.fiap.oficina.domain.cliente.VeiculoRepository;
import java.time.Clock;
import java.time.Year;

public class AtualizarVeiculo {

    private final VeiculoRepository veiculos;
    private final Clock clock;

    public AtualizarVeiculo(VeiculoRepository veiculos, Clock clock) {
        this.veiculos = veiculos;
        this.clock = clock;
    }

    public VeiculoDTO executar(AtualizarVeiculoDTO dto) {
        Veiculo veiculo = veiculos.buscarPorId(dto.id())
                .orElseThrow(() -> new NaoEncontradoException("VEICULO_NAO_ENCONTRADO", "Veiculo nao encontrado"));
        veiculo.atualizar(dto.marca(), dto.modelo(), dto.ano(), Year.now(clock).getValue());
        return VeiculoDTO.de(veiculos.salvar(veiculo));
    }
}
