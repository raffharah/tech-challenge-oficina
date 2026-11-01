package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.CadastroDeVeiculos;
import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;
import java.time.Clock;

public class RejeitarOrcamento {

    private final OrdemServicoRepository ordens;
    private final OrdensDoTitular ordensDoTitular;
    private final Clock clock;

    public RejeitarOrcamento(OrdemServicoRepository ordens, CadastroDeVeiculos veiculos, Clock clock) {
        this.ordens = ordens;
        this.ordensDoTitular = new OrdensDoTitular(ordens, veiculos);
        this.clock = clock;
    }

    public OrdemServicoDTO executar(Long ordemId, String cpfCnpj) {
        OrdemServico ordem = ordensDoTitular.obter(ordemId, cpfCnpj);
        ordem.rejeitar(clock.instant());
        return OrdemServicoDTO.de(ordens.salvar(ordem));
    }
}
