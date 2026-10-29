package br.com.fiap.oficina.domain.ordemservico;

import br.com.fiap.oficina.domain.DomainException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class OrdemServico {

    private final Long id;
    private final Long veiculoId;
    private final String cpfCnpj;
    private final String placa;
    private StatusOrdemServico status;
    private BigDecimal total;
    private final List<ServicoDaOrdem> servicos;
    private final List<ItemDaOrdem> itens;
    private final List<MudancaDeStatus> historico;

    private OrdemServico(Long id, Long veiculoId, String cpfCnpj, String placa, StatusOrdemServico status,
            BigDecimal total, List<ServicoDaOrdem> servicos, List<ItemDaOrdem> itens,
            List<MudancaDeStatus> historico) {
        this.id = id;
        this.veiculoId = veiculoId;
        this.cpfCnpj = cpfCnpj;
        this.placa = placa;
        this.status = status;
        this.total = total;
        this.servicos = new ArrayList<>(servicos);
        this.itens = new ArrayList<>(itens);
        this.historico = new ArrayList<>(historico);
    }

    public static OrdemServico abrir(VeiculoIdentificado veiculo, Instant agora) {
        OrdemServico ordem = new OrdemServico(null, veiculo.veiculoId(), veiculo.cpfCnpj(), veiculo.placa(),
                StatusOrdemServico.RECEBIDA, null, List.of(), List.of(),
                List.of(new MudancaDeStatus(StatusOrdemServico.RECEBIDA, agora)));
        ordem.mudarPara(StatusOrdemServico.EM_DIAGNOSTICO, agora);
        return ordem;
    }

    public static OrdemServico restaurar(Long id, Long veiculoId, String cpfCnpj, String placa,
            StatusOrdemServico status, BigDecimal total, List<ServicoDaOrdem> servicos, List<ItemDaOrdem> itens,
            List<MudancaDeStatus> historico) {
        return new OrdemServico(id, veiculoId, cpfCnpj, placa, status, total, servicos, itens, historico);
    }

    public void incluirServico(Servico servico) {
        exigirEmDiagnostico();
        if (servicos.stream().anyMatch(s -> s.servicoId().equals(servico.getId()))) {
            throw new DomainException("SERVICO_JA_INCLUIDO", "Servico ja incluido na ordem de servico");
        }
        servicos.add(new ServicoDaOrdem(servico.getId(), servico.getNome(), servico.getValorPadrao()));
    }

    public void removerServico(Long servicoId) {
        exigirEmDiagnostico();
        servicos.removeIf(s -> s.servicoId().equals(servicoId));
    }

    public void incluirItem(ItemCotado item, int quantidade) {
        exigirEmDiagnostico();
        if (quantidade <= 0) {
            throw new DomainException("QUANTIDADE_INVALIDA", "Quantidade deve ser maior que zero");
        }
        int posicao = posicaoDoItem(item.itemId());
        if (posicao < 0) {
            itens.add(new ItemDaOrdem(item.itemId(), item.sku(), item.nome(), quantidade, item.precoVenda()));
            return;
        }
        ItemDaOrdem existente = itens.get(posicao);
        itens.set(posicao, new ItemDaOrdem(existente.itemId(), existente.sku(), existente.nome(),
                existente.quantidade() + quantidade, existente.precoUnitario()));
    }

    public void removerItem(Long itemId) {
        exigirEmDiagnostico();
        itens.removeIf(i -> i.itemId().equals(itemId));
    }

    public void concluirDiagnostico(Instant agora) {
        exigirEmDiagnostico();
        if (servicos.isEmpty()) {
            throw new DomainException("ORDEM_SEM_SERVICO", "Ordem de servico precisa de ao menos um servico");
        }
        Orcamento orcamento = Orcamento.calcular(servicos, itens);
        mudarPara(StatusOrdemServico.AGUARDANDO_APROVACAO, agora);
        this.total = orcamento.total();
    }

    public void aprovar(Instant agora) {
        mudarPara(StatusOrdemServico.EM_EXECUCAO, agora);
    }

    public void rejeitar(Instant agora) {
        mudarPara(StatusOrdemServico.CANCELADA, agora);
    }

    public void finalizar(Instant agora) {
        mudarPara(StatusOrdemServico.FINALIZADA, agora);
    }

    public void entregar(Instant agora) {
        mudarPara(StatusOrdemServico.ENTREGUE, agora);
    }

    public Optional<Duration> tempoDeExecucao() {
        Optional<Instant> inicio = quandoMudouPara(StatusOrdemServico.EM_EXECUCAO);
        Optional<Instant> fim = quandoMudouPara(StatusOrdemServico.FINALIZADA);
        if (inicio.isEmpty() || fim.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Duration.between(inicio.get(), fim.get()));
    }

    private Optional<Instant> quandoMudouPara(StatusOrdemServico destino) {
        return historico.stream().filter(m -> m.status() == destino).map(MudancaDeStatus::dataHora).findFirst();
    }

    private void mudarPara(StatusOrdemServico destino, Instant agora) {
        if (!status.podeIrPara(destino)) {
            throw new DomainException("TRANSICAO_INVALIDA", "Transicao de " + status + " para " + destino
                    + " nao permitida");
        }
        this.status = destino;
        historico.add(new MudancaDeStatus(destino, agora));
    }

    private void exigirEmDiagnostico() {
        if (status != StatusOrdemServico.EM_DIAGNOSTICO) {
            throw new DomainException("ORDEM_FORA_DE_DIAGNOSTICO",
                    "Ordem de servico nao esta em diagnostico");
        }
    }

    private int posicaoDoItem(Long itemId) {
        for (int i = 0; i < itens.size(); i++) {
            if (itens.get(i).itemId().equals(itemId)) {
                return i;
            }
        }
        return -1;
    }

    public Long getId() {
        return id;
    }

    public Long getVeiculoId() {
        return veiculoId;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public String getPlaca() {
        return placa;
    }

    public StatusOrdemServico getStatus() {
        return status;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<ServicoDaOrdem> getServicos() {
        return Collections.unmodifiableList(servicos);
    }

    public List<ItemDaOrdem> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public List<MudancaDeStatus> getHistorico() {
        return Collections.unmodifiableList(historico);
    }
}
