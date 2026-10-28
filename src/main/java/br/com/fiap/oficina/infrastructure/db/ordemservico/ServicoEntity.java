package br.com.fiap.oficina.infrastructure.db.ordemservico;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "servico")
class ServicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String nome;

    @Column(name = "valor_padrao", nullable = false, precision = 12, scale = 2)
    BigDecimal valorPadrao;
}
