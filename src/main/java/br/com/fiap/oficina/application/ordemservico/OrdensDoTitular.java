package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.ordemservico.CadastroDeVeiculos;
import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;

// OS de outro titular responde igual a OS inexistente, para não revelar quais números existem
final class OrdensDoTitular {

    private final OrdemServicoRepository ordens;
    private final CadastroDeVeiculos veiculos;

    OrdensDoTitular(OrdemServicoRepository ordens, CadastroDeVeiculos veiculos) {
        this.ordens = ordens;
        this.veiculos = veiculos;
    }

    OrdemServico obter(Long ordemId, String cpfCnpj) {
        String titular = veiculos.normalizarCpfCnpj(cpfCnpj);
        return ordens.buscarPorId(ordemId).filter(o -> o.getCpfCnpj().equals(titular))
                .orElseThrow(() -> new NaoEncontradoException("ORDEM_SERVICO_NAO_ENCONTRADA",
                        "Ordem de servico nao encontrada"));
    }
}
