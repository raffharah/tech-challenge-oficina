package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.CadastroDeVeiculos;
import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;
import java.time.Clock;

public class AbrirOrdemServico {

    private final OrdemServicoRepository ordens;
    private final CadastroDeVeiculos veiculos;
    private final Clock clock;

    public AbrirOrdemServico(OrdemServicoRepository ordens, CadastroDeVeiculos veiculos, Clock clock) {
        this.ordens = ordens;
        this.veiculos = veiculos;
        this.clock = clock;
    }

    public OrdemServicoDTO executar(AbrirOrdemServicoDTO dto) {
        OrdemServico ordem = OrdemServico.abrir(veiculos.identificar(dto.cpfCnpj(), dto.placa()), clock.instant());
        return OrdemServicoDTO.de(ordens.salvar(ordem));
    }
}
