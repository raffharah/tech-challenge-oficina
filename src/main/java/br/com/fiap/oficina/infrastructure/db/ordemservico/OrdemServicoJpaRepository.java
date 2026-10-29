package br.com.fiap.oficina.infrastructure.db.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface OrdemServicoJpaRepository extends JpaRepository<OrdemServicoEntity, Long> {

    List<OrdemServicoEntity> findByStatusIn(Collection<StatusOrdemServico> status);
}
