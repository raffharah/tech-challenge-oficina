package br.com.fiap.oficina.application.ordemservico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import br.com.fiap.oficina.domain.ordemservico.VeiculoIdentificado;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class AbrirOrdemServicoTest {

    private static final Instant AGORA = Instant.parse("2026-01-10T10:00:00Z");

    private final OrdemServicoRepositoryEmMemoria ordens = new OrdemServicoRepositoryEmMemoria();
    private final Clock clock = Clock.fixed(AGORA, ZoneOffset.UTC);

    @Test
    void salvaOrdemEmDiagnostico() {
        AbrirOrdemServico abrir = new AbrirOrdemServico(ordens,
                (cpfCnpj, placa) -> new VeiculoIdentificado(7L, "52998224725", "ABC1D23"), clock);

        OrdemServicoDTO dto = abrir.executar(new AbrirOrdemServicoDTO("529.982.247-25", "abc-1d23"));

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.status()).isEqualTo(StatusOrdemServico.EM_DIAGNOSTICO);
        assertThat(dto.cpfCnpj()).isEqualTo("52998224725");
        assertThat(dto.placa()).isEqualTo("ABC1D23");
        assertThat(dto.historico()).extracting(OrdemServicoDTO.MudancaDeStatusDTO::status)
                .containsExactly(StatusOrdemServico.RECEBIDA, StatusOrdemServico.EM_DIAGNOSTICO);
        assertThat(dto.historico()).extracting(OrdemServicoDTO.MudancaDeStatusDTO::dataHora).containsOnly(AGORA);
        assertThat(ordens.buscarPorId(1L).orElseThrow().getStatus()).isEqualTo(StatusOrdemServico.EM_DIAGNOSTICO);
    }

    @Test
    void dadosInvalidosOuDonoDiferenteLancamDomainExceptionSemSalvar() {
        AbrirOrdemServico abrir = new AbrirOrdemServico(ordens, (cpfCnpj, placa) -> {
            throw new DomainException("CPF_CNPJ_INVALIDO", "CPF/CNPJ invalido");
        }, clock);

        assertThatThrownBy(() -> abrir.executar(new AbrirOrdemServicoDTO("1", "X")))
                .isInstanceOf(DomainException.class).hasMessage("CPF/CNPJ invalido");
        assertThat(ordens.quantidade()).isZero();
    }

    @Test
    void clienteOuVeiculoInexistenteLancaNaoEncontradoSemSalvar() {
        AbrirOrdemServico abrir = new AbrirOrdemServico(ordens, (cpfCnpj, placa) -> {
            throw new NaoEncontradoException("CLIENTE_NAO_ENCONTRADO", "Cliente nao encontrado");
        }, clock);

        assertThatThrownBy(() -> abrir.executar(new AbrirOrdemServicoDTO("52998224725", "ABC1D23")))
                .isInstanceOf(NaoEncontradoException.class);
        assertThat(ordens.quantidade()).isZero();
    }
}
