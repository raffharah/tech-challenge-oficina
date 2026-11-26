package br.com.fiap.oficina.application.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.application.ordemservico.AbrirOrdemServicoDTO.ItemSolicitadoDTO;
import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.ordemservico.CadastroDeVeiculos;
import br.com.fiap.oficina.domain.ordemservico.ItemCotado;
import br.com.fiap.oficina.domain.ordemservico.Servico;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import br.com.fiap.oficina.domain.ordemservico.VeiculoIdentificado;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;

class AbrirOrdemServicoTest {

    private static final Instant AGORA = Instant.parse("2026-01-10T10:00:00Z");

    private final OrdemServicoRepositoryEmMemoria ordens = new OrdemServicoRepositoryEmMemoria();
    private final ServicoRepositoryEmMemoria servicos = new ServicoRepositoryEmMemoria();
    private final EstoqueDeItensEmMemoria estoque = new EstoqueDeItensEmMemoria(
            Map.of(10L, new ItemCotado(10L, "OLE-1", "Oleo 5W30", new BigDecimal("35.50"))));
    private final Clock clock = Clock.fixed(AGORA, ZoneOffset.UTC);
    private final NotificacaoDeStatusEmMemoria notificacao = new NotificacaoDeStatusEmMemoria();
    private final List<String> emails = new ArrayList<>();

    @Test
    void abreOrdemRecebidaComServicosEItens() {
        Long servicoId = servicos.salvar(new Servico(null, "Troca de oleo", new BigDecimal("100.00"))).getId();
        AbrirOrdemServico abrir = abrir((cpfCnpj, placa) -> new VeiculoIdentificado(7L, "52998224725", "ABC1D23"));

        OrdemServicoDTO dto = abrir.executar(dto("529.982.247-25", "abc-1d23", List.of(servicoId), 10L));

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.status()).isEqualTo(StatusOrdemServico.RECEBIDA);
        assertThat(dto.cpfCnpj()).isEqualTo("52998224725");
        assertThat(dto.placa()).isEqualTo("ABC1D23");
        assertThat(dto.servicos()).extracting(OrdemServicoDTO.ServicoDaOrdemDTO::nome).containsExactly("Troca de oleo");
        assertThat(dto.itens()).extracting(OrdemServicoDTO.ItemDaOrdemDTO::quantidade).containsExactly(2);
        assertThat(dto.total()).isNull();
        assertThat(dto.historico()).extracting(OrdemServicoDTO.MudancaDeStatusDTO::dataHora).containsOnly(AGORA);
        assertThat(ordens.buscarPorId(1L).orElseThrow().getStatus()).isEqualTo(StatusOrdemServico.RECEBIDA);
        assertThat(estoque.baixas).isEmpty();
        assertThat(notificacao.notificadas).singleElement().satisfies(o -> {
            assertThat(o.getId()).isEqualTo(1L);
            assertThat(o.getStatus()).isEqualTo(StatusOrdemServico.RECEBIDA);
        });
    }

    @Test
    void registraEmailDoTitular() {
        Long servicoId = servicos.salvar(new Servico(null, "Troca de oleo", new BigDecimal("100.00"))).getId();

        abrir((cpfCnpj, placa) -> new VeiculoIdentificado(7L, "52998224725", "ABC1D23"))
                .executar(dto("52998224725", "ABC1D23", List.of(servicoId), 10L));

        assertThat(emails).containsExactly("52998224725:maria@oficina.com");
    }

    @Test
    void servicoInexistenteLancaNaoEncontradoSemSalvarNemRegistrarEmail() {
        AbrirOrdemServico abrir = abrir((cpfCnpj, placa) -> new VeiculoIdentificado(7L, "52998224725", "ABC1D23"));

        assertThatThrownBy(() -> abrir.executar(dto("52998224725", "ABC1D23", List.of(99L), 10L)))
                .isInstanceOfSatisfying(NaoEncontradoException.class,
                        e -> assertThat(e.getCodigo()).isEqualTo("SERVICO_NAO_ENCONTRADO"));
        assertThat(ordens.quantidade()).isZero();
        assertThat(emails).isEmpty();
        assertThat(notificacao.notificadas).isEmpty();
    }

    @Test
    void itemInexistenteLancaNaoEncontradoSemSalvarNemRegistrarEmail() {
        Long servicoId = servicos.salvar(new Servico(null, "Troca de oleo", new BigDecimal("100.00"))).getId();
        AbrirOrdemServico abrir = abrir((cpfCnpj, placa) -> new VeiculoIdentificado(7L, "52998224725", "ABC1D23"));

        assertThatThrownBy(() -> abrir.executar(dto("52998224725", "ABC1D23", List.of(servicoId), 99L)))
                .isInstanceOfSatisfying(NaoEncontradoException.class,
                        e -> assertThat(e.getCodigo()).isEqualTo("ITEM_NAO_ENCONTRADO"));
        assertThat(ordens.quantidade()).isZero();
        assertThat(emails).isEmpty();
    }

    @Test
    void dadosInvalidosOuDonoDiferenteLancamDomainExceptionSemSalvar() {
        AbrirOrdemServico abrir = abrir((cpfCnpj, placa) -> {
            throw new DomainException("CPF_CNPJ_INVALIDO", "CPF/CNPJ invalido");
        });

        assertThatThrownBy(() -> abrir.executar(dto("1", "X", List.of(1L), 10L)))
                .isInstanceOf(DomainException.class).hasMessage("CPF/CNPJ invalido");
        assertThat(ordens.quantidade()).isZero();
        assertThat(emails).isEmpty();
    }

    @Test
    void clienteOuVeiculoInexistenteLancaNaoEncontradoSemSalvar() {
        AbrirOrdemServico abrir = abrir((cpfCnpj, placa) -> {
            throw new NaoEncontradoException("CLIENTE_NAO_ENCONTRADO", "Cliente nao encontrado");
        });

        assertThatThrownBy(() -> abrir.executar(dto("52998224725", "ABC1D23", List.of(1L), 10L)))
                .isInstanceOf(NaoEncontradoException.class);
        assertThat(ordens.quantidade()).isZero();
    }

    private AbrirOrdemServicoDTO dto(String cpfCnpj, String placa, List<Long> servicoIds, Long itemId) {
        return new AbrirOrdemServicoDTO(cpfCnpj, "maria@oficina.com", placa, servicoIds,
                List.of(new ItemSolicitadoDTO(itemId, 2)));
    }

    private AbrirOrdemServico abrir(BiFunction<String, String, VeiculoIdentificado> identificar) {
        return new AbrirOrdemServico(ordens, new CadastroDeVeiculos() {
            @Override
            public VeiculoIdentificado identificar(String cpfCnpj, String placa) {
                return identificar.apply(cpfCnpj, placa);
            }

            @Override
            public void registrarEmail(String cpfCnpj, String email) {
                emails.add(cpfCnpj + ":" + email);
            }

            @Override
            public String normalizarCpfCnpj(String cpfCnpj) {
                return cpfCnpj;
            }
        }, servicos, estoque, notificacao, clock);
    }
}
