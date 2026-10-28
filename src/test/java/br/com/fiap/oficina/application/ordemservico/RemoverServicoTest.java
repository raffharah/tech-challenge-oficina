package br.com.fiap.oficina.application.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class RemoverServicoTest {

    private final ServicoRepositoryEmMemoria servicos = new ServicoRepositoryEmMemoria();
    private final RemoverServico remover = new RemoverServico(servicos);

    @Test
    void removeServico() {
        ServicoDTO servico = new CadastrarServico(servicos)
                .executar(new CadastrarServicoDTO("Alinhamento", BigDecimal.TEN));

        remover.executar(servico.id());

        assertThat(servicos.quantidade()).isZero();
    }

    @Test
    void inexistenteGeraNaoEncontrado() {
        assertThatThrownBy(() -> remover.executar(99L)).isInstanceOf(NaoEncontradoException.class);
    }
}
