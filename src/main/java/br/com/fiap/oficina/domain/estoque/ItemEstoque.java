package br.com.fiap.oficina.domain.estoque;

import br.com.fiap.oficina.domain.DomainException;
import java.math.BigDecimal;

public class ItemEstoque {

    private final Long id;
    private final String sku;
    private final TipoItem tipo;
    private String nome;
    private BigDecimal precoVenda;
    private int saldoFisico;

    public ItemEstoque(Long id, String sku, String nome, TipoItem tipo, BigDecimal precoVenda, int saldoFisico) {
        if (sku == null || sku.isBlank()) {
            throw new DomainException("SKU_OBRIGATORIO", "SKU do item e obrigatorio");
        }
        if (tipo == null) {
            throw new DomainException("TIPO_OBRIGATORIO", "Tipo do item e obrigatorio");
        }
        if (saldoFisico < 0) {
            throw new DomainException("SALDO_INICIAL_INVALIDO", "Saldo inicial nao pode ser negativo");
        }
        this.id = id;
        this.sku = sku.trim();
        this.tipo = tipo;
        this.nome = nomeValido(nome);
        this.precoVenda = precoVendaValido(precoVenda);
        this.saldoFisico = saldoFisico;
    }

    public void atualizar(String novoNome, BigDecimal novoPrecoVenda) {
        String nomeAlterado = nomeValido(novoNome);
        this.precoVenda = precoVendaValido(novoPrecoVenda);
        this.nome = nomeAlterado;
    }

    public void registrarEntrada(int quantidade) {
        quantidadeValida(quantidade);
        this.saldoFisico += quantidade;
    }

    public void baixar(int quantidade) {
        quantidadeValida(quantidade);
        if (quantidade > saldoFisico) {
            throw new DomainException("SALDO_INSUFICIENTE", "Saldo fisico insuficiente para o item " + sku);
        }
        this.saldoFisico -= quantidade;
    }

    private static String nomeValido(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new DomainException("NOME_OBRIGATORIO", "Nome do item e obrigatorio");
        }
        return nome.trim();
    }

    private static BigDecimal precoVendaValido(BigDecimal precoVenda) {
        if (precoVenda == null || precoVenda.signum() <= 0) {
            throw new DomainException("PRECO_VENDA_INVALIDO", "Preco de venda deve ser maior que zero");
        }
        return precoVenda;
    }

    private static void quantidadeValida(int quantidade) {
        if (quantidade <= 0) {
            throw new DomainException("QUANTIDADE_INVALIDA", "Quantidade deve ser maior que zero");
        }
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getNome() {
        return nome;
    }

    public TipoItem getTipo() {
        return tipo;
    }

    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }

    public int getSaldoFisico() {
        return saldoFisico;
    }
}
