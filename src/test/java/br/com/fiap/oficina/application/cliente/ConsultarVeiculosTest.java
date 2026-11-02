package br.com.fiap.oficina.application.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.Pagina;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class ConsultarVeiculosTest {

    private final ClienteRepositoryEmMemoria clientes = new ClienteRepositoryEmMemoria();
    private final VeiculoRepositoryEmMemoria veiculos = new VeiculoRepositoryEmMemoria();
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneOffset.UTC);
    private final CadastrarVeiculo cadastrar = new CadastrarVeiculo(veiculos, clientes, clock);
    private final ConsultarVeiculos consultar = new ConsultarVeiculos(veiculos);
    private final Long maria = new CadastrarCliente(clientes)
            .executar(new CadastrarClienteDTO("Maria", "52998224725")).id();
    private final Long oficina = new CadastrarCliente(clientes)
            .executar(new CadastrarClienteDTO("Oficina", "11222333000181")).id();

    @Test
    void buscaPorPlacaNaoNormalizada() {
        VeiculoDTO criado = cadastrar.executar(new CadastrarVeiculoDTO("abc-1234", "Fiat", "Uno", 2010, maria));

        assertThat(consultar.buscarPorPlaca("abc-1234")).isEqualTo(criado);
        assertThat(consultar.buscarPorPlaca("ABC1234")).isEqualTo(criado);
    }

    @Test
    void filtraPorPlacaNaoNormalizada() {
        VeiculoDTO criado = cadastrar.executar(new CadastrarVeiculoDTO("abc-1234", "Fiat", "Uno", 2010, maria));
        cadastrar.executar(new CadastrarVeiculoDTO("DEF5678", "VW", "Gol", 2012, oficina));

        assertThat(consultar.listar("abc-1234", 0, 20).itens()).containsExactly(criado);
        assertThat(consultar.listar("ABC1234", 0, 20).total()).isEqualTo(1);
    }

    @Test
    void semFiltroListaTodosPaginado() {
        cadastrar.executar(new CadastrarVeiculoDTO("ABC1234", "Fiat", "Uno", 2010, maria));
        cadastrar.executar(new CadastrarVeiculoDTO("DEF5678", "VW", "Gol", 2012, oficina));
        cadastrar.executar(new CadastrarVeiculoDTO("GHI1J23", "GM", "Onix", 2020, maria));

        Pagina<VeiculoDTO> pagina = consultar.listar(null, 1, 2);

        assertThat(pagina.itens()).extracting(VeiculoDTO::placa).containsExactly("GHI1J23");
        assertThat(pagina.total()).isEqualTo(3);
        assertThat(consultar.listar("", 0, 20).total()).isEqualTo(3);
    }

    @Test
    void filtroSemVeiculoDevolvePaginaVaziaEPlacaInvalidaGeraErroDeDominio() {
        assertThat(consultar.listar("ZZZ9999", 0, 20).itens()).isEmpty();
        assertThatThrownBy(() -> consultar.listar("XX", 0, 20)).isInstanceOf(DomainException.class);
    }

    @Test
    void buscaPorIdRetornaVeiculo() {
        VeiculoDTO criado = cadastrar.executar(new CadastrarVeiculoDTO("ABC1234", "Fiat", "Uno", 2010, maria));

        assertThat(consultar.buscarPorId(criado.id())).isEqualTo(criado);
    }

    @Test
    void listaSoVeiculosDoCliente() {
        cadastrar.executar(new CadastrarVeiculoDTO("ABC1234", "Fiat", "Uno", 2010, maria));
        cadastrar.executar(new CadastrarVeiculoDTO("DEF5678", "VW", "Gol", 2012, oficina));
        cadastrar.executar(new CadastrarVeiculoDTO("GHI1J23", "GM", "Onix", 2020, maria));

        assertThat(consultar.listarPorCliente(maria)).extracting(VeiculoDTO::placa)
                .containsExactly("ABC1234", "GHI1J23");
    }

    @Test
    void inexistenteGeraNaoEncontrado() {
        assertThatThrownBy(() -> consultar.buscarPorPlaca("ZZZ9999")).isInstanceOf(NaoEncontradoException.class);
        assertThatThrownBy(() -> consultar.buscarPorId(99L)).isInstanceOf(NaoEncontradoException.class);
    }
}
