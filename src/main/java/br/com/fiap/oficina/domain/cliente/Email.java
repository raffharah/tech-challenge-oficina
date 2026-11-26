package br.com.fiap.oficina.domain.cliente;

import br.com.fiap.oficina.domain.DomainException;
import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String valor) {

    // Limites da RFC 5321: 254 caracteres no endereço, 64 na parte local
    private static final int TAMANHO_MAXIMO = 254;
    private static final Pattern FORMATO = Pattern.compile("[^@\\s]{1,64}@[^@\\s]+\\.[^@\\s]+");

    public Email {
        valor = valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
        if (valor.length() > TAMANHO_MAXIMO || !FORMATO.matcher(valor).matches()) {
            throw new DomainException("EMAIL_INVALIDO", "E-mail invalido");
        }
    }
}
