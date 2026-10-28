package br.com.fiap.oficina.domain.cliente;

import br.com.fiap.oficina.domain.DomainException;
import java.util.Locale;
import java.util.regex.Pattern;

public record CpfCnpj(String valor) {

    private static final Pattern CPF = Pattern.compile("\\d{11}");
    private static final Pattern CNPJ = Pattern.compile("[0-9A-Z]{12}\\d{2}");
    private static final int[] PESOS_CNPJ = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    public CpfCnpj {
        valor = valor == null ? "" : valor.replaceAll("[^0-9A-Za-z]", "").toUpperCase(Locale.ROOT);
        boolean valido = CPF.matcher(valor).matches() ? cpfValido(valor)
                : CNPJ.matcher(valor).matches() && cnpjValido(valor);
        if (!valido) {
            throw new DomainException("CPF_CNPJ_INVALIDO", "CPF/CNPJ invalido");
        }
    }

    private static boolean cpfValido(String cpf) {
        if (todosIguais(cpf)) {
            return false;
        }
        return digitoCpf(cpf, 9) == cpf.charAt(9) - '0' && digitoCpf(cpf, 10) == cpf.charAt(10) - '0';
    }

    private static int digitoCpf(String cpf, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += (cpf.charAt(i) - '0') * (tamanho + 1 - i);
        }
        int resto = soma * 10 % 11;
        return resto == 10 ? 0 : resto;
    }

    private static boolean cnpjValido(String cnpj) {
        if (cnpj.chars().allMatch(Character::isDigit) && todosIguais(cnpj)) {
            return false;
        }
        return digitoCnpj(cnpj, 12) == cnpj.charAt(12) - '0' && digitoCnpj(cnpj, 13) == cnpj.charAt(13) - '0';
    }

    private static int digitoCnpj(String cnpj, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += (cnpj.charAt(i) - '0') * PESOS_CNPJ[PESOS_CNPJ.length - tamanho + i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static boolean todosIguais(String texto) {
        return texto.chars().distinct().count() == 1;
    }
}
