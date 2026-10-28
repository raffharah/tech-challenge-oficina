package br.com.fiap.oficina.domain.cliente;

import br.com.fiap.oficina.domain.DomainException;
import java.util.Locale;
import java.util.regex.Pattern;

public record Placa(String valor) {

    private static final Pattern FORMATO = Pattern.compile("[A-Z]{3}\\d([A-Z]|\\d)\\d{2}");

    public Placa {
        valor = valor == null ? "" : valor.replace("-", "").trim().toUpperCase(Locale.ROOT);
        if (!FORMATO.matcher(valor).matches()) {
            throw new DomainException("PLACA_INVALIDA", "Placa invalida");
        }
    }
}
