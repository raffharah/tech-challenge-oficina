package br.com.fiap.oficina.domain.cliente;

import br.com.fiap.oficina.domain.DomainException;

public class Cliente {

    private final Long id;
    private String nome;
    private final CpfCnpj cpfCnpj;

    public Cliente(Long id, String nome, CpfCnpj cpfCnpj) {
        this.id = id;
        this.nome = nomeValido(nome);
        this.cpfCnpj = cpfCnpj;
    }

    public void renomear(String novoNome) {
        this.nome = nomeValido(novoNome);
    }

    private static String nomeValido(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new DomainException("NOME_OBRIGATORIO", "Nome do cliente e obrigatorio");
        }
        return nome.trim();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public CpfCnpj getCpfCnpj() {
        return cpfCnpj;
    }
}
