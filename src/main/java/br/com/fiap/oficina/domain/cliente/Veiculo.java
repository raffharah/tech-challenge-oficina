package br.com.fiap.oficina.domain.cliente;

import br.com.fiap.oficina.domain.DomainException;

public class Veiculo {

    private static final int ANO_MINIMO = 1900;

    private final Long id;
    private final Placa placa;
    private String marca;
    private String modelo;
    private int ano;
    private final Long clienteId;

    public Veiculo(Long id, Placa placa, String marca, String modelo, int ano, Long clienteId, int anoCorrente) {
        this.id = id;
        this.placa = placa;
        this.clienteId = clienteId;
        atualizar(marca, modelo, ano, anoCorrente);
    }

    public void atualizar(String novaMarca, String novoModelo, int novoAno, int anoCorrente) {
        if (novaMarca == null || novaMarca.isBlank() || novoModelo == null || novoModelo.isBlank()) {
            throw new DomainException("VEICULO_INVALIDO", "Marca e modelo do veiculo sao obrigatorios");
        }
        if (novoAno < ANO_MINIMO || novoAno > anoCorrente + 1) {
            throw new DomainException("ANO_VEICULO_INVALIDO",
                    "Ano do veiculo deve estar entre " + ANO_MINIMO + " e " + (anoCorrente + 1));
        }
        this.marca = novaMarca.trim();
        this.modelo = novoModelo.trim();
        this.ano = novoAno;
    }

    public Long getId() {
        return id;
    }

    public Placa getPlaca() {
        return placa;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public int getAno() {
        return ano;
    }

    public Long getClienteId() {
        return clienteId;
    }
}
