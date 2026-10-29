package br.com.fiap.oficina.domain.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrcamentoTest {

    @Test
    void somaServicosEItens() {
        var servicos = List.of(new ServicoDaOrdem(1L, "Troca de oleo", new BigDecimal("100.00")));
        var itens = List.of(new ItemDaOrdem(1L, "OLE-1", "Oleo", 2, new BigDecimal("35.50")));

        assertThat(Orcamento.calcular(servicos, itens).total()).isEqualTo(new BigDecimal("171.00"));
    }

    @Test
    void arredondaHalfEvenComTresCasas() {
        var par = List.of(new ServicoDaOrdem(1L, "A", new BigDecimal("10.005")));
        var impar = List.of(new ServicoDaOrdem(1L, "A", new BigDecimal("10.015")));

        assertThat(Orcamento.calcular(par, List.of()).total()).isEqualTo(new BigDecimal("10.00"));
        assertThat(Orcamento.calcular(impar, List.of()).total()).isEqualTo(new BigDecimal("10.02"));
    }

    @Test
    void semItensSomaSoServicos() {
        var servicos = List.of(new ServicoDaOrdem(1L, "A", new BigDecimal("80")),
                new ServicoDaOrdem(2L, "B", new BigDecimal("20.5")));

        assertThat(Orcamento.calcular(servicos, List.of()).total()).isEqualTo(new BigDecimal("100.50"));
    }
}
