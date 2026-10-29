package br.com.fiap.oficina.application.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.Pagina;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ConsultarServicosTest {

    private final ServicoRepositoryEmMemoria servicos = new ServicoRepositoryEmMemoria();
    private final CadastrarServico cadastrar = new CadastrarServico(servicos);
    private final ConsultarServicos consultar = new ConsultarServicos(servicos);

    @Test
    void buscaPorId() {
        ServicoDTO criado = cadastrar.executar(new CadastrarServicoDTO("Alinhamento", BigDecimal.TEN));

        assertThat(consultar.buscarPorId(criado.id())).isEqualTo(criado);
    }

    @Test
    void buscaPorIdInexistenteGeraNaoEncontrado() {
        assertThatThrownBy(() -> consultar.buscarPorId(1L)).isInstanceOf(NaoEncontradoException.class);
    }

    @Test
    void listaPaginada() {
        cadastrar.executar(new CadastrarServicoDTO("Alinhamento", BigDecimal.TEN));
        cadastrar.executar(new CadastrarServicoDTO("Balanceamento", BigDecimal.TEN));
        cadastrar.executar(new CadastrarServicoDTO("Troca de oleo", BigDecimal.TEN));

        Pagina<ServicoDTO> pagina = consultar.listar(1, 2);

        assertThat(pagina.itens()).extracting(ServicoDTO::nome).containsExactly("Troca de oleo");
        assertThat(pagina.total()).isEqualTo(3);
        assertThat(pagina.pagina()).isEqualTo(1);
        assertThat(pagina.tamanho()).isEqualTo(2);
    }
}
