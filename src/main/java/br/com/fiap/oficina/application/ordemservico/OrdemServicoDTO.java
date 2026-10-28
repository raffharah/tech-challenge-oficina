package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrdemServicoDTO(Long id, String cpfCnpj, String placa, StatusOrdemServico status,
        List<ServicoDaOrdemDTO> servicos, List<ItemDaOrdemDTO> itens, BigDecimal total,
        List<MudancaDeStatusDTO> historico) {

    public record ServicoDaOrdemDTO(Long servicoId, String nome, BigDecimal valor) {
    }

    public record ItemDaOrdemDTO(Long itemId, String sku, String nome, int quantidade, BigDecimal precoUnitario) {
    }

    public record MudancaDeStatusDTO(StatusOrdemServico status, Instant dataHora) {
    }

    static OrdemServicoDTO de(OrdemServico ordem) {
        return new OrdemServicoDTO(ordem.getId(), ordem.getCpfCnpj(), ordem.getPlaca(), ordem.getStatus(),
                ordem.getServicos().stream()
                        .map(s -> new ServicoDaOrdemDTO(s.servicoId(), s.nome(), s.valor())).toList(),
                ordem.getItens().stream()
                        .map(i -> new ItemDaOrdemDTO(i.itemId(), i.sku(), i.nome(), i.quantidade(),
                                i.precoUnitario()))
                        .toList(),
                ordem.getTotal(),
                ordem.getHistorico().stream().map(m -> new MudancaDeStatusDTO(m.status(), m.dataHora())).toList());
    }
}
