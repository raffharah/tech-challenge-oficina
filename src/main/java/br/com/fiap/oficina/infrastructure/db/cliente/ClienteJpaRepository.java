package br.com.fiap.oficina.infrastructure.db.cliente;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface ClienteJpaRepository extends JpaRepository<ClienteEntity, Long> {

    Optional<ClienteEntity> findByCpfCnpj(String cpfCnpj);
}
