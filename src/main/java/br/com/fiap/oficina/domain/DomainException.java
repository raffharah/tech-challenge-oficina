package br.com.fiap.oficina.domain;

import java.util.List;

public class DomainException extends RuntimeException {

    private final String codigo;
    private final List<?> detalhes;

    public DomainException(String codigo, String mensagem) {
        this(codigo, mensagem, null);
    }

    public DomainException(String codigo, String mensagem, List<?> detalhes) {
        super(mensagem);
        this.codigo = codigo;
        this.detalhes = detalhes;
    }

    public String getCodigo() {
        return codigo;
    }

    public List<?> getDetalhes() {
        return detalhes;
    }
}
