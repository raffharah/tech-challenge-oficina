package br.com.fiap.oficina.application.cliente;

import br.com.fiap.oficina.domain.cliente.Veiculo;

public record VeiculoDTO(Long id, String placa, String marca, String modelo, int ano, Long clienteId) {

    static VeiculoDTO de(Veiculo veiculo) {
        return new VeiculoDTO(veiculo.getId(), veiculo.getPlaca().valor(), veiculo.getMarca(), veiculo.getModelo(),
                veiculo.getAno(), veiculo.getClienteId());
    }
}
