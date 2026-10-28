package br.com.fiap.oficina.infrastructure.db.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "ordem_servico")
class OrdemServicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "veiculo_id", nullable = false)
    Long veiculoId;

    @Column(name = "cpf_cnpj", nullable = false)
    String cpfCnpj;

    @Column(nullable = false)
    String placa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    StatusOrdemServico status;

    @Column(precision = 12, scale = 2)
    BigDecimal total;

    @Column(name = "aberta_em", nullable = false)
    Instant abertaEm;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "ordem_servico_servico", joinColumns = @JoinColumn(name = "ordem_servico_id"))
    @OrderBy("servicoId")
    List<ServicoEmbutido> servicos = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "ordem_servico_item", joinColumns = @JoinColumn(name = "ordem_servico_id"))
    @OrderBy("itemId")
    List<ItemEmbutido> itens = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    @OrderBy("id")
    List<MudancaDeStatusEntity> historico = new ArrayList<>();

    @Embeddable
    static class ServicoEmbutido {

        @Column(name = "servico_id", nullable = false)
        Long servicoId;

        @Column(nullable = false)
        String nome;

        @Column(nullable = false, precision = 12, scale = 2)
        BigDecimal valor;
    }

    @Embeddable
    static class ItemEmbutido {

        @Column(name = "item_id", nullable = false)
        Long itemId;

        @Column(nullable = false)
        String sku;

        @Column(nullable = false)
        String nome;

        @Column(nullable = false)
        int quantidade;

        @Column(name = "preco_unitario", nullable = false, precision = 12, scale = 2)
        BigDecimal precoUnitario;
    }

    @Entity
    @Table(name = "ordem_servico_historico")
    static class MudancaDeStatusEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        Long id;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        StatusOrdemServico status;

        @Column(name = "data_hora", nullable = false)
        Instant dataHora;
    }
}
