package br.com.fiap.oficina.application.ordemservico;

import java.util.List;

public record AbrirOrdemServicoDTO(String cpfCnpj, String email, String placa, List<Long> servicos,
        List<ItemSolicitadoDTO> itens) {

    public record ItemSolicitadoDTO(Long itemId, int quantidade) {
    }
}
