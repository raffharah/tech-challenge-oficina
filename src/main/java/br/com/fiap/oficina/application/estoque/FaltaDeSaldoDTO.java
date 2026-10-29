package br.com.fiap.oficina.application.estoque;

public record FaltaDeSaldoDTO(String sku, int quantidadePedida, int saldoFisico) {
}
