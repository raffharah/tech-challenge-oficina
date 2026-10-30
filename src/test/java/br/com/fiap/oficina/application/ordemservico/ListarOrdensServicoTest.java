package br.com.fiap.oficina.application.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.fiap.oficina.domain.Pagina;
import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import br.com.fiap.oficina.domain.ordemservico.VeiculoIdentificado;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ListarOrdensServicoTest {

    private final OrdemServicoRepositoryEmMemoria ordens = new OrdemServicoRepositoryEmMemoria();
    private final ListarOrdensServico listar = new ListarOrdensServico(ordens);

    @Test
    void listaDaMaisNovaParaAMaisAntigaComCamposDoResumo() {
        ordens.salvar(OrdemServico.abrir(new VeiculoIdentificado(7L, "52998224725", "ABC1D23"),
                Instant.parse("2026-01-10T10:00:00Z")));
        ordens.salvar(OrdemServico.abrir(new VeiculoIdentificado(8L, "11144477735", "XYZ9K88"),
                Instant.parse("2026-01-11T10:00:00Z")));

        Pagina<OrdemServicoResumoDTO> pagina = listar.executar(0, 10);

        assertThat(pagina.total()).isEqualTo(2);
        assertThat(pagina.itens()).containsExactly(
                new OrdemServicoResumoDTO(2L, "11144477735", "XYZ9K88", StatusOrdemServico.EM_DIAGNOSTICO, null),
                new OrdemServicoResumoDTO(1L, "52998224725", "ABC1D23", StatusOrdemServico.EM_DIAGNOSTICO, null));
    }
}
