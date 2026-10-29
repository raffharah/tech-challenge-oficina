package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.Pagina;
import br.com.fiap.oficina.domain.ordemservico.ServicoRepository;

public class ConsultarServicos {

    private final ServicoRepository servicos;

    public ConsultarServicos(ServicoRepository servicos) {
        this.servicos = servicos;
    }

    public ServicoDTO buscarPorId(Long id) {
        return servicos.buscarPorId(id).map(ServicoDTO::de)
                .orElseThrow(() -> new NaoEncontradoException("SERVICO_NAO_ENCONTRADO", "Servico nao encontrado"));
    }

    public Pagina<ServicoDTO> listar(int pagina, int tamanho) {
        return servicos.listar(pagina, tamanho).map(ServicoDTO::de);
    }
}
