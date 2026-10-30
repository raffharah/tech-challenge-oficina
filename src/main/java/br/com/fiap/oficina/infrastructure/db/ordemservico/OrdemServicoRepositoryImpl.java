package br.com.fiap.oficina.infrastructure.db.ordemservico;

import br.com.fiap.oficina.domain.Pagina;
import br.com.fiap.oficina.domain.ordemservico.ItemDaOrdem;
import br.com.fiap.oficina.domain.ordemservico.MudancaDeStatus;
import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;
import br.com.fiap.oficina.domain.ordemservico.ServicoDaOrdem;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import br.com.fiap.oficina.infrastructure.db.ordemservico.OrdemServicoEntity.ItemEmbutido;
import br.com.fiap.oficina.infrastructure.db.ordemservico.OrdemServicoEntity.MudancaDeStatusEntity;
import br.com.fiap.oficina.infrastructure.db.ordemservico.OrdemServicoEntity.ServicoEmbutido;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class OrdemServicoRepositoryImpl implements OrdemServicoRepository {

    private final OrdemServicoJpaRepository jpa;

    OrdemServicoRepositoryImpl(OrdemServicoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public OrdemServico salvar(OrdemServico ordem) {
        OrdemServicoEntity entity = new OrdemServicoEntity();
        entity.id = ordem.getId();
        entity.veiculoId = ordem.getVeiculoId();
        entity.cpfCnpj = ordem.getCpfCnpj();
        entity.placa = ordem.getPlaca();
        entity.status = ordem.getStatus();
        entity.total = ordem.getTotal();
        entity.abertaEm = ordem.getHistorico().get(0).dataHora();
        ordem.getServicos().forEach(s -> entity.servicos.add(paraEmbutido(s)));
        ordem.getItens().forEach(i -> entity.itens.add(paraEmbutido(i)));
        ordem.getHistorico().forEach(m -> entity.historico.add(paraEntity(m)));
        return paraModelo(jpa.saveAndFlush(entity));
    }

    @Override
    public Optional<OrdemServico> buscarPorId(Long id) {
        return jpa.findById(id).map(OrdemServicoRepositoryImpl::paraModelo);
    }

    @Override
    public Pagina<OrdemServico> listar(int pagina, int tamanho) {
        Page<OrdemServicoEntity> page = jpa.findAll(
                PageRequest.of(pagina, tamanho, Sort.by(Sort.Order.desc("abertaEm"), Sort.Order.desc("id"))));
        return new Pagina<>(page.map(OrdemServicoRepositoryImpl::paraModelo).getContent(), pagina, tamanho,
                page.getTotalElements());
    }

    @Override
    public List<OrdemServico> listarFinalizadas() {
        return jpa.findByStatusIn(List.of(StatusOrdemServico.FINALIZADA, StatusOrdemServico.ENTREGUE)).stream()
                .map(OrdemServicoRepositoryImpl::paraModelo).toList();
    }

    private static ServicoEmbutido paraEmbutido(ServicoDaOrdem servico) {
        ServicoEmbutido embutido = new ServicoEmbutido();
        embutido.servicoId = servico.servicoId();
        embutido.nome = servico.nome();
        embutido.valor = servico.valor();
        return embutido;
    }

    private static ItemEmbutido paraEmbutido(ItemDaOrdem item) {
        ItemEmbutido embutido = new ItemEmbutido();
        embutido.itemId = item.itemId();
        embutido.sku = item.sku();
        embutido.nome = item.nome();
        embutido.quantidade = item.quantidade();
        embutido.precoUnitario = item.precoUnitario();
        return embutido;
    }

    private static MudancaDeStatusEntity paraEntity(MudancaDeStatus mudanca) {
        MudancaDeStatusEntity entity = new MudancaDeStatusEntity();
        entity.status = mudanca.status();
        entity.dataHora = mudanca.dataHora();
        return entity;
    }

    private static OrdemServico paraModelo(OrdemServicoEntity entity) {
        return OrdemServico.restaurar(entity.id, entity.veiculoId, entity.cpfCnpj, entity.placa, entity.status,
                entity.total,
                entity.servicos.stream().map(s -> new ServicoDaOrdem(s.servicoId, s.nome, s.valor)).toList(),
                entity.itens.stream()
                        .map(i -> new ItemDaOrdem(i.itemId, i.sku, i.nome, i.quantidade, i.precoUnitario)).toList(),
                entity.historico.stream().map(m -> new MudancaDeStatus(m.status, m.dataHora)).toList());
    }
}
