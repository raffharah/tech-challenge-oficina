package br.com.fiap.oficina.infrastructure.gateway.estoque;

import br.com.fiap.oficina.application.estoque.BaixarItens;
import br.com.fiap.oficina.application.estoque.ConsultarItens;
import br.com.fiap.oficina.application.estoque.ItemBaixaDTO;
import br.com.fiap.oficina.application.estoque.ItemEstoqueDTO;
import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.ordemservico.EstoqueDeItens;
import br.com.fiap.oficina.domain.ordemservico.ItemCotado;
import br.com.fiap.oficina.domain.ordemservico.ItemDaOrdem;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EstoqueDeItensImpl implements EstoqueDeItens {

    private final ConsultarItens itens;

    private final BaixarItens baixarItens;

    EstoqueDeItensImpl(ConsultarItens itens, BaixarItens baixarItens) {
        this.itens = itens;
        this.baixarItens = baixarItens;
    }

    @Override
    public ItemCotado cotar(Long itemId) {
        try {
            ItemEstoqueDTO item = itens.buscarPorId(itemId);
            return new ItemCotado(item.id(), item.sku(), item.nome(), item.precoVenda());
        } catch (NaoEncontradoException e) {
            throw new DomainException(e.getCodigo(), e.getMessage());
        }
    }

    @Override
    public void baixar(List<ItemDaOrdem> itensDaOrdem) {
        try {
            baixarItens.executar(itensDaOrdem.stream()
                    .map(item -> new ItemBaixaDTO(item.itemId(), item.quantidade()))
                    .toList());
        } catch (NaoEncontradoException e) {
            throw new DomainException(e.getCodigo(), e.getMessage());
        }
    }
}
