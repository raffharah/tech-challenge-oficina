package br.com.fiap.oficina.infrastructure.gateway.estoque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.ordemservico.EstoqueDeItens;
import br.com.fiap.oficina.domain.ordemservico.ItemCotado;
import br.com.fiap.oficina.infrastructure.TestcontainersConfig;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Import(TestcontainersConfig.class)
class EstoqueDeItensImplIT {

    @Autowired
    EstoqueDeItens estoque;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void prepararBanco() {
        jdbc.execute("truncate table ordem_servico, veiculo, cliente, servico, item_estoque restart identity cascade");
        jdbc.update("insert into item_estoque (sku, nome, tipo, preco_venda, saldo_fisico) values ('OLE-1', 'Oleo 5W30', 'INSUMO', 35.50, 10)");
    }

    @Test
    void cotaComSkuNomeEPrecoDeVenda() {
        ItemCotado cotado = estoque.cotar(1L);

        assertThat(cotado.itemId()).isEqualTo(1L);
        assertThat(cotado.sku()).isEqualTo("OLE-1");
        assertThat(cotado.nome()).isEqualTo("Oleo 5W30");
        assertThat(cotado.precoVenda()).isEqualByComparingTo(new BigDecimal("35.50"));
    }

    @Test
    void itemInexistenteLancaDomainExceptionDe422() {
        assertThatThrownBy(() -> estoque.cotar(99L)).isExactlyInstanceOf(DomainException.class);
    }
}
