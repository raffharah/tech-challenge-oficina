package br.com.fiap.oficina.application.estoque;

import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.estoque.ItemEstoque;
import br.com.fiap.oficina.domain.estoque.ItemEstoqueRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BaixarItens {

    private final ItemEstoqueRepository itens;

    public BaixarItens(ItemEstoqueRepository itens) {
        this.itens = itens;
    }

    public void executar(List<ItemBaixaDTO> baixas) {
        Map<Long, Integer> pedidos = new LinkedHashMap<>();
        baixas.forEach(baixa -> pedidos.merge(baixa.itemId(), baixa.quantidade(), Integer::sum));
        Map<Long, ItemEstoque> encontrados = new LinkedHashMap<>();
        itens.buscarPorIds(pedidos.keySet()).forEach(item -> encontrados.put(item.getId(), item));
        pedidos.keySet().forEach(id -> {
            if (!encontrados.containsKey(id)) {
                throw new NaoEncontradoException("ITEM_NAO_ENCONTRADO", "Item nao encontrado: " + id);
            }
        });

        List<FaltaDeSaldoDTO> faltas = pedidos.entrySet().stream()
                .filter(pedido -> pedido.getValue() > encontrados.get(pedido.getKey()).getSaldoFisico())
                .map(pedido -> new FaltaDeSaldoDTO(encontrados.get(pedido.getKey()).getSku(), pedido.getValue(),
                        encontrados.get(pedido.getKey()).getSaldoFisico()))
                .toList();
        if (!faltas.isEmpty()) {
            throw new DomainException("SALDO_INSUFICIENTE", "Saldo fisico insuficiente para a baixa", faltas);
        }

        pedidos.forEach((id, quantidade) -> encontrados.get(id).baixar(quantidade));
        encontrados.values().forEach(itens::salvar);
    }
}
