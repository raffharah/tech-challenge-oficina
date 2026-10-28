package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ConflitoException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.ordemservico.Servico;
import br.com.fiap.oficina.domain.ordemservico.ServicoRepository;

public class AtualizarServico {

    private final ServicoRepository servicos;

    public AtualizarServico(ServicoRepository servicos) {
        this.servicos = servicos;
    }

    public ServicoDTO executar(AtualizarServicoDTO dto) {
        Servico servico = servicos.buscarPorId(dto.id())
                .orElseThrow(() -> new NaoEncontradoException("SERVICO_NAO_ENCONTRADO", "Servico nao encontrado"));
        servico.alterar(dto.nome(), dto.valorPadrao());
        servicos.buscarPorNome(servico.getNome())
                .filter(existente -> !existente.getId().equals(servico.getId()))
                .ifPresent(existente -> {
                    throw new ConflitoException("SERVICO_DUPLICADO", "Ja existe servico com este nome");
                });
        return ServicoDTO.de(servicos.salvar(servico));
    }
}
