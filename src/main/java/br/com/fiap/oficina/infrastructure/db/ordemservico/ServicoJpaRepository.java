package br.com.fiap.oficina.infrastructure.db.ordemservico;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface ServicoJpaRepository extends JpaRepository<ServicoEntity, Long> {

    Optional<ServicoEntity> findByNomeIgnoreCase(String nome);
}
