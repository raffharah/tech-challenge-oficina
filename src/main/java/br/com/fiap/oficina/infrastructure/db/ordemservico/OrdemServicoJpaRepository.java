package br.com.fiap.oficina.infrastructure.db.ordemservico;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrdemServicoJpaRepository extends JpaRepository<OrdemServicoEntity, Long> {
}
