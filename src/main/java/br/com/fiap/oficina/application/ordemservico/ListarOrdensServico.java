package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.Pagina;
import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;

public class ListarOrdensServico {

    private final OrdemServicoRepository ordens;

    public ListarOrdensServico(OrdemServicoRepository ordens) {
        this.ordens = ordens;
    }

    public Pagina<OrdemServicoResumoDTO> executar(int pagina, int tamanho) {
        return ordens.listar(pagina, tamanho).map(OrdemServicoResumoDTO::de);
    }
}
