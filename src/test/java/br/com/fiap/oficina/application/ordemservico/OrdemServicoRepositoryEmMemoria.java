package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;
import java.util.LinkedHashMap;
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

    private static OrdemServico copia(Long id, OrdemServico o) {
        return OrdemServico.restaurar(id, o.getVeiculoId(), o.getCpfCnpj(), o.getPlaca(), o.getStatus(), o.getTotal(),
                o.getServicos(), o.getItens(), o.getHistorico());
    }

    int quantidade() {
        return armazenadas.size();
    }
}
