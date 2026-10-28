package br.com.fiap.oficina.infrastructure.db.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.Pagina;
import br.com.fiap.oficina.domain.ordemservico.Servico;
import br.com.fiap.oficina.domain.ordemservico.ServicoRepository;
import br.com.fiap.oficina.infrastructure.TestcontainersConfig;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Import(TestcontainersConfig.class)
class ServicoRepositoryImplIT {

    @Autowired
    ServicoRepository servicos;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void limparBanco() {
        jdbc.execute("truncate table servico restart identity cascade");
    }

    @Test
    void salvaEBuscaPorIdENomeIgnorandoCaixa() {
        Servico salvo = servicos.salvar(new Servico(null, "Troca de oleo", new BigDecimal("120.50")));

        assertThat(salvo.getId()).isNotNull();
        Servico buscado = servicos.buscarPorId(salvo.getId()).orElseThrow();
        assertThat(buscado.getNome()).isEqualTo("Troca de oleo");
        assertThat(buscado.getValorPadrao()).isEqualByComparingTo("120.50");
        assertThat(servicos.buscarPorNome("TROCA DE OLEO").orElseThrow().getId()).isEqualTo(salvo.getId());
        assertThat(servicos.buscarPorNome("Alinhamento")).isEmpty();
    }

    @Test
    void alteraMantendoId() {
        Servico salvo = servicos.salvar(new Servico(null, "Alinhamento", BigDecimal.TEN));
        salvo.alterar("Alinhamento 3D", new BigDecimal("90.00"));

        Servico alterado = servicos.salvar(salvo);

        assertThat(alterado.getId()).isEqualTo(salvo.getId());
        Servico buscado = servicos.buscarPorId(salvo.getId()).orElseThrow();
        assertThat(buscado.getNome()).isEqualTo("Alinhamento 3D");
        assertThat(buscado.getValorPadrao()).isEqualByComparingTo("90.00");
    }

    @Test
    void listaPaginado() {
        servicos.salvar(new Servico(null, "Alinhamento", BigDecimal.TEN));
        servicos.salvar(new Servico(null, "Balanceamento", BigDecimal.TEN));
        servicos.salvar(new Servico(null, "Troca de oleo", BigDecimal.TEN));

        Pagina<Servico> pagina = servicos.listar(1, 2);

        assertThat(pagina.itens()).extracting(Servico::getNome).containsExactly("Troca de oleo");
        assertThat(pagina.total()).isEqualTo(3);
        assertThat(pagina.tamanho()).isEqualTo(2);
    }

    @Test
    void removeServico() {
        Servico salvo = servicos.salvar(new Servico(null, "Alinhamento", BigDecimal.TEN));

        servicos.remover(salvo.getId());

        assertThat(servicos.buscarPorId(salvo.getId())).isEmpty();
    }

    @Test
    void nomeDuplicadoEmOutraCaixaViolaConstraint() {
        servicos.salvar(new Servico(null, "Alinhamento", BigDecimal.TEN));

        assertThatThrownBy(() -> servicos.salvar(new Servico(null, "ALINHAMENTO", BigDecimal.TEN)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void valorPadraoNaoPositivoViolaConstraint() {
        assertThatThrownBy(() -> jdbc.update("insert into servico (nome, valor_padrao) values ('X', 0)"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
