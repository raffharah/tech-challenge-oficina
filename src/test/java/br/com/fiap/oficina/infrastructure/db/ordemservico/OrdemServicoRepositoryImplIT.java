package br.com.fiap.oficina.infrastructure.db.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.ordemservico.ItemCotado;
import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;
import br.com.fiap.oficina.domain.ordemservico.Servico;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import br.com.fiap.oficina.domain.ordemservico.VeiculoIdentificado;
import br.com.fiap.oficina.infrastructure.TestcontainersConfig;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Import(TestcontainersConfig.class)
class OrdemServicoRepositoryImplIT {

    private static final Instant ABERTURA = Instant.parse("2026-01-10T10:00:00Z");
    private static final Instant CONCLUSAO = Instant.parse("2026-01-10T12:00:00Z");

    @Autowired
    OrdemServicoRepository ordens;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void prepararBanco() {
        jdbc.execute("truncate table ordem_servico, veiculo, cliente, servico, item_estoque restart identity cascade");
        jdbc.update("insert into cliente (nome, cpf_cnpj) values ('Maria', '52998224725')");
        jdbc.update("insert into veiculo (placa, marca, modelo, ano, cliente_id) values ('ABC1D23', 'Fiat', 'Uno', 2020, 1)");
        jdbc.update("insert into servico (nome, valor_padrao) values ('Troca de oleo', 100.00)");
        jdbc.update("insert into item_estoque (sku, nome, tipo, preco_venda, saldo_fisico) values ('OLE-1', 'Oleo', 'INSUMO', 35.50, 10)");
    }

    private OrdemServico ordemCompleta() {
        OrdemServico ordem = OrdemServico.abrir(new VeiculoIdentificado(1L, "52998224725", "ABC1D23"), ABERTURA);
        ordem.incluirServico(new Servico(1L, "Troca de oleo", new BigDecimal("100.00")));
        ordem.incluirItem(new ItemCotado(1L, "OLE-1", "Oleo", new BigDecimal("35.50")), 2);
        ordem.concluirDiagnostico(CONCLUSAO);
        return ordem;
    }

    @Test
    void salvaERecarregaComComponentesTotalEHistoricoEmOrdem() {
        OrdemServico salva = ordens.salvar(ordemCompleta());

        OrdemServico lida = ordens.buscarPorId(salva.getId()).orElseThrow();

        assertThat(lida.getVeiculoId()).isEqualTo(1L);
        assertThat(lida.getCpfCnpj()).isEqualTo("52998224725");
        assertThat(lida.getPlaca()).isEqualTo("ABC1D23");
        assertThat(lida.getStatus()).isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO);
        assertThat(lida.getTotal()).isEqualByComparingTo("171.00");
        assertThat(lida.getServicos()).singleElement().satisfies(s -> {
            assertThat(s.servicoId()).isEqualTo(1L);
            assertThat(s.nome()).isEqualTo("Troca de oleo");
            assertThat(s.valor()).isEqualByComparingTo("100.00");
        });
        assertThat(lida.getItens()).singleElement().satisfies(i -> {
            assertThat(i.itemId()).isEqualTo(1L);
            assertThat(i.sku()).isEqualTo("OLE-1");
            assertThat(i.quantidade()).isEqualTo(2);
            assertThat(i.precoUnitario()).isEqualByComparingTo("35.50");
        });
        assertThat(lida.getHistorico()).extracting(m -> m.status()).containsExactly(
                StatusOrdemServico.RECEBIDA, StatusOrdemServico.EM_DIAGNOSTICO,
                StatusOrdemServico.AGUARDANDO_APROVACAO);
        assertThat(lida.getHistorico().get(2).dataHora()).isEqualTo(CONCLUSAO);
    }

    @Test
    void atualizaMantendoIdERemovendoComponente() {
        OrdemServico salva = ordens.salvar(OrdemServico.abrir(new VeiculoIdentificado(1L, "52998224725", "ABC1D23"), ABERTURA));
        salva.incluirItem(new ItemCotado(1L, "OLE-1", "Oleo", new BigDecimal("35.50")), 2);
        ordens.salvar(salva);
        OrdemServico recarregada = ordens.buscarPorId(salva.getId()).orElseThrow();
        recarregada.removerItem(1L);

        ordens.salvar(recarregada);

        OrdemServico lida = ordens.buscarPorId(salva.getId()).orElseThrow();
        assertThat(lida.getId()).isEqualTo(salva.getId());
        assertThat(lida.getItens()).isEmpty();
        assertThat(lida.getHistorico()).hasSize(2);
    }

    @Test
    void removerVeiculoServicoOuItemReferenciadoViolaIntegridade() {
        ordens.salvar(ordemCompleta());

        assertThatThrownBy(() -> jdbc.update("delete from veiculo where id = 1"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("delete from servico where id = 1"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("delete from item_estoque where id = 1"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void listaPaginadoPorAberturaDecrescente() {
        VeiculoIdentificado veiculo = new VeiculoIdentificado(1L, "52998224725", "ABC1D23");
        ordens.salvar(OrdemServico.abrir(veiculo, ABERTURA));
        ordens.salvar(OrdemServico.abrir(veiculo, ABERTURA.plusSeconds(3600)));
        ordens.salvar(OrdemServico.abrir(veiculo, ABERTURA.plusSeconds(7200)));

        var primeira = ordens.listar(0, 2);
        var segunda = ordens.listar(1, 2);

        assertThat(primeira.total()).isEqualTo(3);
        assertThat(primeira.itens()).extracting(OrdemServico::getId).containsExactly(3L, 2L);
        assertThat(segunda.itens()).extracting(OrdemServico::getId).containsExactly(1L);
    }

    @Test
    void listarFinalizadasIgnoraOutrosStatus() {
        OrdemServico emExecucao = ordemCompleta();
        emExecucao.aprovar(CONCLUSAO.plusSeconds(60));
        OrdemServico finalizada = ordemCompleta();
        finalizada.aprovar(CONCLUSAO.plusSeconds(60));
        finalizada.finalizar(CONCLUSAO.plusSeconds(3660));
        OrdemServico entregue = ordemCompleta();
        entregue.aprovar(CONCLUSAO.plusSeconds(60));
        entregue.finalizar(CONCLUSAO.plusSeconds(7260));
        entregue.entregar(CONCLUSAO.plusSeconds(8000));
        ordens.salvar(ordemCompleta());
        ordens.salvar(emExecucao);
        ordens.salvar(finalizada);
        ordens.salvar(entregue);

        var lidas = ordens.listarFinalizadas();

        assertThat(lidas).extracting(OrdemServico::getStatus)
                .containsExactlyInAnyOrder(StatusOrdemServico.FINALIZADA, StatusOrdemServico.ENTREGUE);
    }
}
