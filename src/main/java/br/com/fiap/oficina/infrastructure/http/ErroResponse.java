package br.com.fiap.oficina.infrastructure.http;

import java.util.List;

public record ErroResponse(String codigo, String mensagem, List<CampoInvalido> campos, List<?> detalhes) {

    public record CampoInvalido(String campo, String mensagem) {
    }
}
