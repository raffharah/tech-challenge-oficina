package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.ordemservico.CadastroDeVeiculos;
import br.com.fiap.oficina.domain.ordemservico.EstoqueDeItens;
import br.com.fiap.oficina.domain.ordemservico.ItemSolicitado;
import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;
import br.com.fiap.oficina.domain.ordemservico.Servico;
import br.com.fiap.oficina.domain.ordemservico.ServicoRepository;
import br.com.fiap.oficina.domain.ordemservico.VeiculoIdentificado;
import java.time.Clock;
import java.util.List;

public class AbrirOrdemServico {

    private final OrdemServicoRepository ordens;
    private final CadastroDeVeiculos veiculos;
    private final ServicoRepository servicos;
    private final EstoqueDeItens estoque;
    private final Clock clock;

    public AbrirOrdemServico(OrdemServicoRepository ordens, CadastroDeVeiculos veiculos, ServicoRepository servicos,
            EstoqueDeItens estoque, Clock clock) {
        this.ordens = ordens;
        this.veiculos = veiculos;
        this.servicos = servicos;
        this.estoque = estoque;
        this.clock = clock;
    }

    public OrdemServicoDTO executar(AbrirOrdemServicoDTO dto) {
        VeiculoIdentificado veiculo = veiculos.identificar(dto.cpfCnpj(), dto.placa());
        List<Servico> solicitados = dto.servicos().stream()
                .map(id -> servicos.buscarPorId(id).orElseThrow(
                        () -> new NaoEncontradoException("SERVICO_NAO_ENCONTRADO", "Servico nao encontrado")))
                .toList();
        List<ItemSolicitado> itens = dto.itens().stream()
                .map(i -> new ItemSolicitado(estoque.cotar(i.itemId()), i.quantidade())).toList();
        OrdemServico ordem = OrdemServico.abrir(veiculo, solicitados, itens, clock.instant());
        veiculos.registrarEmail(dto.cpfCnpj(), dto.email());
        return OrdemServicoDTO.de(ordens.salvar(ordem));
    }
}
