package br.com.fiap.oficina.infrastructure.gateway.notificacao;

import br.com.fiap.oficina.application.cliente.ClienteDTO;
import br.com.fiap.oficina.application.cliente.ConsultarClientes;
import br.com.fiap.oficina.domain.ordemservico.NotificacaoDeStatus;
import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class NotificacaoDeStatusImpl implements NotificacaoDeStatus {

    private static final Logger LOG = LoggerFactory.getLogger(NotificacaoDeStatusImpl.class);

    private final ConsultarClientes clientes;
    private final JavaMailSender mail;
    private final String remetente;

    NotificacaoDeStatusImpl(ConsultarClientes clientes, JavaMailSender mail,
            @Value("${oficina.notificacao.remetente}") String remetente) {
        this.clientes = clientes;
        this.mail = mail;
        this.remetente = remetente;
    }

    @Override
    public void notificar(OrdemServico ordem) {
        ClienteDTO cliente = clientes.buscarPorCpfCnpj(ordem.getCpfCnpj());
        if (cliente.email() == null) {
            return;
        }
        SimpleMailMessage mensagem = montar(ordem, cliente.email());
        Long numero = ordem.getId();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviar(mensagem, numero);
                }
            });
        } else {
            enviar(mensagem, numero);
        }
    }

    private SimpleMailMessage montar(OrdemServico ordem, String destino) {
        StringBuilder corpo = new StringBuilder("Ordem de servico ").append(ordem.getId())
                .append("\nVeiculo: ").append(ordem.getPlaca())
                .append("\nStatus: ").append(ordem.getStatus());
        if (ordem.getStatus() == StatusOrdemServico.AGUARDANDO_APROVACAO) {
            corpo.append("\nTotal do orcamento: ").append(ordem.getTotal());
        }
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destino);
        mensagem.setSubject("OS " + ordem.getId() + ": " + ordem.getStatus());
        mensagem.setText(corpo.toString());
        return mensagem;
    }

    private void enviar(SimpleMailMessage mensagem, Long numero) {
        try {
            mail.send(mensagem);
        } catch (MailException e) {
            LOG.error("Falha ao enviar e-mail de status da OS {}", numero, e);
        }
    }
}
