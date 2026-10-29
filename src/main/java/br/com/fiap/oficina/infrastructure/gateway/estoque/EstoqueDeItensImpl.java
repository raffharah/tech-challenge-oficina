package br.com.fiap.oficina.infrastructure.gateway.estoque;

import br.com.fiap.oficina.application.estoque.ConsultarItens;
import br.com.fiap.oficina.application.estoque.ItemEstoqueDTO;
import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.ordemservico.EstoqueDeItens;
import br.com.fiap.oficina.domain.ordemservico.ItemCotado;
import org.springframework.stereotype.Component;

@Component
public class EstoqueDeItensImpl implements EstoqueDeItens {

    private final ConsultarItens itens;

    EstoqueDeItensImpl(ConsultarItens itens) {
        this.itens = itens;
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
}
