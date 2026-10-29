package br.com.fiap.oficina.application.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.ConflitoException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class AtualizarServicoTest {

    private final ServicoRepositoryEmMemoria servicos = new ServicoRepositoryEmMemoria();
    private final CadastrarServico cadastrar = new CadastrarServico(servicos);
    private final AtualizarServico atualizar = new AtualizarServico(servicos);

    @Test
    void alteraNomeEValorPadrao() {
        ServicoDTO criado = cadastrar.executar(new CadastrarServicoDTO("Alinhamento", BigDecimal.TEN));

        ServicoDTO dto = atualizar.executar(
                new AtualizarServicoDTO(criado.id(), "Alinhamento 3D", new BigDecimal("90.50")));

        assertThat(dto.id()).isEqualTo(criado.id());
        assertThat(dto.nome()).isEqualTo("Alinhamento 3D");
        assertThat(dto.valorPadrao()).isEqualByComparingTo("90.50");
        assertThat(servicos.buscarPorId(criado.id()).orElseThrow().getValorPadrao()).isEqualByComparingTo("90.50");
    }

    @Test
    void manterProprioNomeEmOutraCaixaPermitido() {
        ServicoDTO criado = cadastrar.executar(new CadastrarServicoDTO("Alinhamento", BigDecimal.TEN));

        ServicoDTO dto = atualizar.executar(new AtualizarServicoDTO(criado.id(), "ALINHAMENTO", BigDecimal.ONE));

        assertThat(dto.nome()).isEqualTo("ALINHAMENTO");
    }

    @Test
    void nomeDeOutroServicoGeraConflito() {
        cadastrar.executar(new CadastrarServicoDTO("Alinhamento", BigDecimal.TEN));
        ServicoDTO outro = cadastrar.executar(new CadastrarServicoDTO("Balanceamento", BigDecimal.TEN));

        assertThatThrownBy(() -> atualizar.executar(
                new AtualizarServicoDTO(outro.id(), "alinhamento", BigDecimal.ONE)))
                .isInstanceOf(ConflitoException.class);
        assertThat(servicos.buscarPorId(outro.id()).orElseThrow().getNome()).isEqualTo("Balanceamento");
    }

    @Test
    void inexistenteGeraNaoEncontrado() {
        assertThatThrownBy(() -> atualizar.executar(new AtualizarServicoDTO(99L, "Alinhamento", BigDecimal.TEN)))
                .isInstanceOf(NaoEncontradoException.class);
    }
}
