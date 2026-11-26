package br.com.fiap.oficina.infrastructure.db.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface OrdemServicoJpaRepository extends JpaRepository<OrdemServicoEntity, Long> {

    Page<OrdemServicoEntity> findByCpfCnpj(String cpfCnpj, Pageable pageable);

    @Query(value = """
            select o from OrdemServicoEntity o
            where o.status not in (br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico.FINALIZADA,
                                   br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico.ENTREGUE)
            order by case o.status
                when br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico.EM_EXECUCAO then 0
                when br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico.AGUARDANDO_APROVACAO then 1
                when br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico.EM_DIAGNOSTICO then 2
                when br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico.RECEBIDA then 3
                else 4 end, o.abertaEm, o.id""",
            countQuery = """
            select count(o) from OrdemServicoEntity o
            where o.status not in (br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico.FINALIZADA,
                                   br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico.ENTREGUE)""")
    Page<OrdemServicoEntity> listarPorPrioridade(Pageable pageable);

    List<OrdemServicoEntity> findByStatusIn(Collection<StatusOrdemServico> status);
}
