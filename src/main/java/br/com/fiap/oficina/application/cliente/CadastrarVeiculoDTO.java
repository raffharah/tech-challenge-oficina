package br.com.fiap.oficina.application.cliente;

public record CadastrarVeiculoDTO(String placa, String marca, String modelo, int ano, Long clienteId) {
}
