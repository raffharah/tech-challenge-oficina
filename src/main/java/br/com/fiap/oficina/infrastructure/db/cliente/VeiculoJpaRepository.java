package br.com.fiap.oficina.infrastructure.db.cliente;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface VeiculoJpaRepository extends JpaRepository<VeiculoEntity, Long> {

    Optional<VeiculoEntity> findByPlaca(String placa);

    List<VeiculoEntity> findByClienteIdOrderById(Long clienteId);

    boolean existsByClienteId(Long clienteId);
}
