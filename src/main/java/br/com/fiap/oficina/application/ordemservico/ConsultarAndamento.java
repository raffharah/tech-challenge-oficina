package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.ordemservico.CadastroDeVeiculos;
import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;

public class ConsultarAndamento {

    private final OrdemServicoRepository ordens;
    private final CadastroDeVeiculos veiculos;

    public ConsultarAndamento(OrdemServicoRepository ordens, CadastroDeVeiculos veiculos) {
        this.ordens = ordens;
        this.veiculos = veiculos;
    }

    public OrdemServicoDTO executar(Long ordemId, String cpfCnpj) {
        String titular = veiculos.normalizarCpfCnpj(cpfCnpj);
        return ordens.buscarPorId(ordemId).filter(o -> o.getCpfCnpj().equals(titular)).map(OrdemServicoDTO::de)
                .orElseThrow(() -> new NaoEncontradoException("ORDEM_SERVICO_NAO_ENCONTRADA",
                        "Ordem de servico nao encontrada"));
    }
}
