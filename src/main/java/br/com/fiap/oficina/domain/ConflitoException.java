package br.com.fiap.oficina.domain;

public class ConflitoException extends DomainException {

    public ConflitoException(String codigo, String mensagem) {
        super(codigo, mensagem);
    }
}
