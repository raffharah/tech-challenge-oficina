package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;
import br.com.fiap.oficina.domain.Pagina;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class OrdemServicoRepositoryEmMemoria implements OrdemServicoRepository {

    private final Map<Long, OrdemServico> armazenadas = new LinkedHashMap<>();
    private long sequencia = 0;

    @Override
    public OrdemServico salvar(OrdemServico ordem) {
        Long id = ordem.getId() == null ? ++sequencia : ordem.getId();
        armazenadas.put(id, copia(id, ordem));
        return copia(id, ordem);
    }

    @Override
    public Optional<OrdemServico> buscarPorId(Long id) {
        return Optional.ofNullable(armazenadas.get(id)).map(o -> copia(id, o));
    }

    @Override
    public Pagina<OrdemServico> listar(int pagina, int tamanho) {
        List<OrdemServico> maisNovasPrimeiro = armazenadas.keySet().stream()
                .map(id -> copia(id, armazenadas.get(id)))
                .sorted(Comparator.comparing((OrdemServico o) -> o.getHistorico().get(0).dataHora()).reversed()
                        .thenComparing(OrdemServico::getId, Comparator.reverseOrder()))
                .toList();
        return new Pagina<>(maisNovasPrimeiro.stream().skip((long) pagina * tamanho).limit(tamanho).toList(),
                pagina, tamanho, maisNovasPrimeiro.size());
    }

    @Override
    public List<OrdemServico> listarFinalizadas() {
        return armazenadas.keySet().stream().map(id -> copia(id, armazenadas.get(id)))
                .filter(o -> o.tempoDeExecucao().isPresent()).toList();
    }

    private static OrdemServico copia(Long id, OrdemServico o) {
        return OrdemServico.restaurar(id, o.getVeiculoId(), o.getCpfCnpj(), o.getPlaca(), o.getStatus(), o.getTotal(),
                o.getServicos(), o.getItens(), o.getHistorico());
    }

    int quantidade() {
        return armazenadas.size();
    }
}
