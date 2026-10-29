package br.com.fiap.oficina.domain.ordemservico;

public enum StatusOrdemServico {
    RECEBIDA,
    EM_DIAGNOSTICO,
    AGUARDANDO_APROVACAO,
    EM_EXECUCAO,
    FINALIZADA,
    ENTREGUE,
    CANCELADA;

    public boolean podeIrPara(StatusOrdemServico destino) {
        return switch (this) {
            case RECEBIDA -> destino == EM_DIAGNOSTICO;
            case EM_DIAGNOSTICO -> destino == AGUARDANDO_APROVACAO;
            case AGUARDANDO_APROVACAO -> destino == EM_EXECUCAO || destino == CANCELADA;
            case EM_EXECUCAO -> destino == FINALIZADA;
            case FINALIZADA -> destino == ENTREGUE;
            case ENTREGUE, CANCELADA -> false;
        };
    }
}
