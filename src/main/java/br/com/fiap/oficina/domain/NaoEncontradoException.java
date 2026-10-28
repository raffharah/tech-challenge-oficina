package br.com.fiap.oficina.domain;

public class NaoEncontradoException extends DomainException {

    public NaoEncontradoException(String codigo, String mensagem) {
        super(codigo, mensagem);
    }
}
