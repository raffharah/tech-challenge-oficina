package br.com.fiap.oficina.infrastructure.db.cliente;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "veiculo")
class VeiculoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    String placa;

    @Column(nullable = false)
    String marca;

    @Column(nullable = false)
    String modelo;

    @Column(nullable = false)
    int ano;

    @Column(name = "cliente_id", nullable = false)
    Long clienteId;
}
