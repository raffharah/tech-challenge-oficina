package br.com.fiap.oficina.domain.ordemservico;

import static br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico.AGUARDANDO_APROVACAO;
import static br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico.CANCELADA;
import static br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico.EM_DIAGNOSTICO;
import static br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico.EM_EXECUCAO;
import static br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico.ENTREGUE;
import static br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico.FINALIZADA;
import static br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico.RECEBIDA;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StatusOrdemServicoTest {

    private static final Set<Arguments> PERMITIDAS = Set.of(
            Arguments.of(RECEBIDA, EM_DIAGNOSTICO),
            Arguments.of(EM_DIAGNOSTICO, AGUARDANDO_APROVACAO),
            Arguments.of(AGUARDANDO_APROVACAO, EM_EXECUCAO),
            Arguments.of(AGUARDANDO_APROVACAO, CANCELADA),
            Arguments.of(EM_EXECUCAO, FINALIZADA),
            Arguments.of(FINALIZADA, ENTREGUE));

    static Stream<Arguments> combinacoes() {
        return Stream.of(StatusOrdemServico.values())
                .flatMap(origem -> Stream.of(StatusOrdemServico.values()).map(destino -> Arguments.of(origem, destino)));
    }

    @ParameterizedTest
    @MethodSource("combinacoes")
    void permiteSomenteTransicoesDoCicloDeVida(StatusOrdemServico origem, StatusOrdemServico destino) {
        boolean esperado = PERMITIDAS.stream()
                .anyMatch(p -> p.get()[0] == origem && p.get()[1] == destino);

        assertThat(origem.podeIrPara(destino)).isEqualTo(esperado);
    }

    @Test
    void combinacoesSao49ComSeisPermitidas() {
        assertThat(combinacoes().count()).isEqualTo(49);
        assertThat(PERMITIDAS).hasSize(6);
    }
}
