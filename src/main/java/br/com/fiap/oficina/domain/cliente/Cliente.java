package br.com.fiap.oficina.domain.cliente;

import br.com.fiap.oficina.domain.DomainException;

public class Cliente {

    private final Long id;
    private String nome;
    private final CpfCnpj cpfCnpj;
    private Email email;

    public Cliente(Long id, String nome, CpfCnpj cpfCnpj, Email email) {
        this.id = id;
        this.nome = nomeValido(nome);
        this.cpfCnpj = cpfCnpj;
        this.email = email;
    }

    public void renomear(String novoNome) {
        this.nome = nomeValido(novoNome);
    }

    public void registrarEmail(Email novoEmail) {
        this.email = novoEmail;
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

    public Email getEmail() {
        return email;
    }
}
