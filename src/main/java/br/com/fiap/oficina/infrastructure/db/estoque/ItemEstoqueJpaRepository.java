package br.com.fiap.oficina.infrastructure.db.estoque;

import org.springframework.data.jpa.repository.JpaRepository;

interface ItemEstoqueJpaRepository extends JpaRepository<ItemEstoqueEntity, Long> {

    boolean existsBySkuIgnoreCase(String sku);
}
