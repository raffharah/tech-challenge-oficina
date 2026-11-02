package br.com.fiap.oficina.application.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.fiap.oficina.domain.Pagina;
import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import br.com.fiap.oficina.domain.ordemservico.VeiculoIdentificado;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ListarOrdensDoClienteTest {

    private final OrdemServicoRepositoryEmMemoria ordens = new OrdemServicoRepositoryEmMemoria();
    private final ListarOrdensDoCliente listar = new ListarOrdensDoCliente(ordens,
            new CadastroDeVeiculosSoNormaliza());

    private void abrir(String cpfCnpj, String placa, String instante) {
        ordens.salvar(OrdemServico.abrir(new VeiculoIdentificado(7L, cpfCnpj, placa), Instant.parse(instante)));
    }

    @Test
    void listaSoAsOrdensDoTitularDaMaisNovaParaAMaisAntiga() {
        abrir("52998224725", "ABC1D23", "2026-01-10T10:00:00Z");
        abrir("11144477735", "XYZ9K88", "2026-01-11T10:00:00Z");
        abrir("52998224725", "ABC1D23", "2026-01-12T10:00:00Z");

        Pagina<OrdemServicoResumoDTO> pagina = listar.executar("529.982.247-25", 0, 20);

        assertThat(pagina.total()).isEqualTo(2);
        assertThat(pagina.itens()).containsExactly(
                new OrdemServicoResumoDTO(3L, "52998224725", "ABC1D23", StatusOrdemServico.RECEBIDA, null,
                        Instant.parse("2026-01-12T10:00:00Z")),
                new OrdemServicoResumoDTO(1L, "52998224725", "ABC1D23", StatusOrdemServico.RECEBIDA, null,
                        Instant.parse("2026-01-10T10:00:00Z")));
    }

    @Test
    void paginaAsOrdensDoTitular() {
        abrir("52998224725", "ABC1D23", "2026-01-10T10:00:00Z");
        abrir("52998224725", "ABC1D23", "2026-01-11T10:00:00Z");
        abrir("52998224725", "ABC1D23", "2026-01-12T10:00:00Z");

        Pagina<OrdemServicoResumoDTO> pagina = listar.executar("52998224725", 1, 2);

        assertThat(pagina.total()).isEqualTo(3);
        assertThat(pagina.itens()).extracting(OrdemServicoResumoDTO::id).containsExactly(1L);
    }

    @Test
    void titularSemOrdensRecebePaginaVazia() {
        abrir("11144477735", "XYZ9K88", "2026-01-11T10:00:00Z");

        Pagina<OrdemServicoResumoDTO> pagina = listar.executar("52998224725", 0, 20);

        assertThat(pagina.itens()).isEmpty();
        assertThat(pagina.total()).isZero();
    }
}
