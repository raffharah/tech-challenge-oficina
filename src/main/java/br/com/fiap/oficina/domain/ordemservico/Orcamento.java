package br.com.fiap.oficina.domain.ordemservico;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record Orcamento(BigDecimal total) {

    public static Orcamento calcular(List<ServicoDaOrdem> servicos, List<ItemDaOrdem> itens) {
        BigDecimal total = BigDecimal.ZERO;
        for (ServicoDaOrdem servico : servicos) {
            total = total.add(servico.valor());
        }
        for (ItemDaOrdem item : itens) {
            total = total.add(item.precoUnitario().multiply(BigDecimal.valueOf(item.quantidade())));
        }
        return new Orcamento(total.setScale(2, RoundingMode.HALF_EVEN));
    }
}
