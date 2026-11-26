package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.NotificacaoDeStatus;
import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import java.util.ArrayList;
import java.util.List;

class NotificacaoDeStatusEmMemoria implements NotificacaoDeStatus {

    final List<OrdemServico> notificadas = new ArrayList<>();

    @Override
    public void notificar(OrdemServico ordem) {
        notificadas.add(ordem);
    }
}
