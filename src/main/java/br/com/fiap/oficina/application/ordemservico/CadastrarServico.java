package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ConflitoException;
import br.com.fiap.oficina.domain.ordemservico.Servico;
import br.com.fiap.oficina.domain.ordemservico.ServicoRepository;

public class CadastrarServico {

    private final ServicoRepository servicos;

    public CadastrarServico(ServicoRepository servicos) {
        this.servicos = servicos;
    }

    public ServicoDTO executar(CadastrarServicoDTO dto) {
        Servico servico = new Servico(null, dto.nome(), dto.valorPadrao());
        if (servicos.buscarPorNome(servico.getNome()).isPresent()) {
            throw new ConflitoException("SERVICO_DUPLICADO", "Ja existe servico com este nome");
        }
        return ServicoDTO.de(servicos.salvar(servico));
    }
}
