package br.com.fiap.oficina.domain.ordemservico;

import br.com.fiap.oficina.domain.DomainException;
import java.math.BigDecimal;

public class Servico {

    private final Long id;
    private String nome;
    private BigDecimal valorPadrao;

    public Servico(Long id, String nome, BigDecimal valorPadrao) {
        this.id = id;
        this.nome = nomeValido(nome);
        this.valorPadrao = valorPadraoValido(valorPadrao);
    }

    public void alterar(String novoNome, BigDecimal novoValorPadrao) {
        String nomeAlterado = nomeValido(novoNome);
        this.valorPadrao = valorPadraoValido(novoValorPadrao);
        this.nome = nomeAlterado;
    }

    private static String nomeValido(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new DomainException("NOME_OBRIGATORIO", "Nome do servico e obrigatorio");
        }
        return nome.trim();
    }

    private static BigDecimal valorPadraoValido(BigDecimal valorPadrao) {
        if (valorPadrao == null || valorPadrao.signum() <= 0) {
            throw new DomainException("VALOR_PADRAO_INVALIDO", "Valor padrao do servico deve ser maior que zero");
        }
        return valorPadrao;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getValorPadrao() {
        return valorPadrao;
    }
}
