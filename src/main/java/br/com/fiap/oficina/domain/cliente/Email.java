package br.com.fiap.oficina.domain.cliente;

import br.com.fiap.oficina.domain.DomainException;
import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String valor) {

    private static final Pattern FORMATO = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

    public Email {
        valor = valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
        if (!FORMATO.matcher(valor).matches()) {
            throw new DomainException("EMAIL_INVALIDO", "E-mail invalido");
        }
    }
}
