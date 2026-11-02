package br.com.fiap.oficina.infrastructure.db.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface OrdemServicoJpaRepository extends JpaRepository<OrdemServicoEntity, Long> {

    Page<OrdemServicoEntity> findByCpfCnpj(String cpfCnpj, Pageable pageable);

    List<OrdemServicoEntity> findByStatusIn(Collection<StatusOrdemServico> status);
}
