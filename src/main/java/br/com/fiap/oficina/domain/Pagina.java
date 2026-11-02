package br.com.fiap.oficina.domain;

import java.util.List;
import java.util.function.Function;

public record Pagina<T>(List<T> itens, int pagina, int tamanho, long total) {

    public static <T> Pagina<T> de(List<T> todos, int pagina, int tamanho) {
        int de = Math.min(pagina * tamanho, todos.size());
        int ate = Math.min(de + tamanho, todos.size());
        return new Pagina<>(todos.subList(de, ate), pagina, tamanho, todos.size());
    }

    public <R> Pagina<R> map(Function<T, R> conversor) {
        return new Pagina<>(itens.stream().map(conversor).toList(), pagina, tamanho, total);
    }
}
