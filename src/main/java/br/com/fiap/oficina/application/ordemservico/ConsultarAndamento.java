package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.CadastroDeVeiculos;
import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;

public class ConsultarAndamento {

    private final OrdensDoTitular ordensDoTitular;

    public ConsultarAndamento(OrdemServicoRepository ordens, CadastroDeVeiculos veiculos) {
        this.ordensDoTitular = new OrdensDoTitular(ordens, veiculos);
    }

    public OrdemServicoDTO executar(Long ordemId, String cpfCnpj) {
        return OrdemServicoDTO.de(ordensDoTitular.obter(ordemId, cpfCnpj));
    }
}
