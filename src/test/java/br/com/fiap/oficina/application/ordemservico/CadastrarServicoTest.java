package br.com.fiap.oficina.application.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.ConflitoException;
import br.com.fiap.oficina.domain.DomainException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CadastrarServicoTest {

    private final ServicoRepositoryEmMemoria servicos = new ServicoRepositoryEmMemoria();
    private final CadastrarServico cadastrar = new CadastrarServico(servicos);

    @Test
    void cadastraServicoComValorPadrao() {
        ServicoDTO dto = cadastrar.executar(new CadastrarServicoDTO("Troca de oleo", new BigDecimal("120.00")));

        assertThat(dto.id()).isNotNull();
        assertThat(dto.nome()).isEqualTo("Troca de oleo");
        assertThat(dto.valorPadrao()).isEqualByComparingTo("120.00");
        assertThat(servicos.quantidade()).isEqualTo(1);
    }

    @Test
    void valorPadraoInvalidoNaoPersiste() {
        assertThatThrownBy(() -> cadastrar.executar(new CadastrarServicoDTO("Troca de oleo", BigDecimal.ZERO)))
                .isInstanceOf(DomainException.class)
                .isNotInstanceOf(ConflitoException.class);
        assertThat(servicos.quantidade()).isZero();
    }

    @Test
    void nomeRepetidoEmOutraCaixaGeraConflito() {
        cadastrar.executar(new CadastrarServicoDTO("Troca de oleo", BigDecimal.TEN));

        assertThatThrownBy(() -> cadastrar.executar(new CadastrarServicoDTO("TROCA DE OLEO", BigDecimal.ONE)))
                .isInstanceOf(ConflitoException.class);
        assertThat(servicos.quantidade()).isEqualTo(1);
    }
}
