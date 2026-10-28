package br.com.fiap.oficina.infrastructure.db.estoque;

import br.com.fiap.oficina.domain.estoque.TipoItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;

@Entity
@Table(name = "item_estoque")
class ItemEstoqueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String sku;

    @Column(nullable = false)
    String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TipoItem tipo;

    @Column(name = "preco_venda", nullable = false, precision = 12, scale = 2)
    BigDecimal precoVenda;

    @Column(name = "saldo_fisico", nullable = false)
    int saldoFisico;

    @Version
    @Column(nullable = false)
    Long versao;
}
