package br.com.fiap.oficina.application.cliente;

import br.com.fiap.oficina.domain.ConflitoException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.cliente.ClienteRepository;
import br.com.fiap.oficina.domain.cliente.Placa;
import br.com.fiap.oficina.domain.cliente.Veiculo;
import br.com.fiap.oficina.domain.cliente.VeiculoRepository;
import java.time.Clock;
import java.time.Year;

public class CadastrarVeiculo {

    private final VeiculoRepository veiculos;
    private final ClienteRepository clientes;
    private final Clock clock;

    public CadastrarVeiculo(VeiculoRepository veiculos, ClienteRepository clientes, Clock clock) {
        this.veiculos = veiculos;
        this.clientes = clientes;
        this.clock = clock;
    }

    public VeiculoDTO executar(CadastrarVeiculoDTO dto) {
        Veiculo veiculo = new Veiculo(null, new Placa(dto.placa()), dto.marca(), dto.modelo(), dto.ano(),
                dto.clienteId(), Year.now(clock).getValue());
        if (clientes.buscarPorId(dto.clienteId()).isEmpty()) {
            throw new NaoEncontradoException("CLIENTE_NAO_ENCONTRADO", "Cliente nao encontrado");
        }
        if (veiculos.buscarPorPlaca(veiculo.getPlaca()).isPresent()) {
            throw new ConflitoException("VEICULO_DUPLICADO", "Ja existe veiculo com esta placa");
        }
        return VeiculoDTO.de(veiculos.salvar(veiculo));
    }
}
