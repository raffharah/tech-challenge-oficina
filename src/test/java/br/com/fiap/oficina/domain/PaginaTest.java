package br.com.fiap.oficina.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class PaginaTest {

    @Test
    void recortaAPaginaPedidaComTotalDaLista() {
        Pagina<String> pagina = Pagina.de(List.of("a", "b", "c"), 1, 2);

        assertThat(pagina.itens()).containsExactly("c");
        assertThat(pagina.pagina()).isEqualTo(1);
        assertThat(pagina.tamanho()).isEqualTo(2);
        assertThat(pagina.total()).isEqualTo(3);
    }

    @Test
    void paginaAlemDoFimVemVaziaMantendoTotal() {
        Pagina<String> pagina = Pagina.de(List.of("a"), 3, 20);

        assertThat(pagina.itens()).isEmpty();
        assertThat(pagina.total()).isEqualTo(1);
    }
}
