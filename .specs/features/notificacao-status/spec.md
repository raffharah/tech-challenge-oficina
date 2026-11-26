# Notificação de Status por E-mail Specification

## Problem Statement

A fase 2 pede a atualização de status da OS por uma ferramenta como e-mail (`docs/fase2-tech-challenge.md:34`). Hoje o cliente só vê o status consultando a API, e e-mail está fora do MVP (`docs/ddd/linguagem-ubiqua.md`, Decisões do MVP). O e-mail do cliente passa a ser gravado na abertura da OS (`abertura-os-completa`).

## Goals

- [ ] NTF-01: toda mudança de status da OS envia e-mail ao cliente titular.
- [ ] NTF-02: falha no envio não desfaz nem bloqueia a mudança de status.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Notificação ao funcionário | O requisito trata da atualização ao cliente |
| Outros canais (SMS, WhatsApp, push) | E-mail atende o requisito |
| Reenvio automático de e-mail com falha | Sem fila; a falha fica em log |
| Templates HTML | Texto simples com número da OS, placa e status atende |
| E-mail no CRUD de clientes | O e-mail entra pela abertura da OS |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Quais mudanças notificam | Todas, inclusive a abertura (`RECEBIDA`) e `CANCELADA` | Decisão do usuário | y |
| Origem do e-mail | E-mail do cliente gravado na última abertura de OS | Decisão do usuário | y |
| Momento do envio | Depois do commit da transação da mudança; o caso de uso chama a porta e o gateway adia o envio para depois do commit | Não enviar e-mail de mudança que sofreu rollback; `domain` e `application` seguem sem Spring | y |
| Servidor de e-mail local | Mailpit no `docker-compose`; SMTP configurável por variáveis de ambiente | Mostra os e-mails no vídeo sem provedor real | y |
| Remetente | Configuração `oficina.notificacao.remetente` | Endereço muda por ambiente | y |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Enviar e-mail a cada mudança de status ⭐ MVP

**User Story**: Como cliente, quero receber e-mail quando minha OS muda de status, para acompanhar o serviço sem consultar a API.

**Why P1**: Requisito obrigatório da fase 2.

**Acceptance Criteria**:

1. WHEN a OS é aberta THEN o sistema SHALL enviar ao e-mail do titular uma mensagem com número da OS, placa e status `RECEBIDA`.
2. WHEN a OS muda para `EM_DIAGNOSTICO`, `AGUARDANDO_APROVACAO`, `EM_EXECUCAO`, `FINALIZADA`, `ENTREGUE` ou `CANCELADA` THEN o sistema SHALL enviar ao e-mail do titular uma mensagem com número da OS, placa e novo status.
3. WHEN a OS vai para `AGUARDANDO_APROVACAO` THEN o sistema SHALL incluir o total do orçamento na mensagem.
4. IF a mudança de status falha (regra violada ou rollback) THEN o sistema SHALL não enviar e-mail.
5. The sistema SHALL enviar o e-mail só depois do commit da transação que gravou a mudança de status.
6. IF o envio do e-mail falha THEN o sistema SHALL manter a mudança de status, responder como responderia sem a falha e registrar o erro em log com o número da OS.
7. The sistema SHALL enviar o e-mail por uma porta do `domain` implementada em `infrastructure/gateway`, sem dependência de Spring em `domain` e `application`.

**Independent Test**: Levar uma OS de `RECEBIDA` a `ENTREGUE` e conferir 6 e-mails no servidor SMTP de teste; com o SMTP fora do ar, cada passo responde 200.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| NTF-01 | P1: Enviar e-mail a cada mudança de status | - | Pending |
| NTF-02 | P1: Enviar e-mail a cada mudança de status | - | Pending |

**Coverage:** 2 total, 0 mapped to tasks, 2 unmapped

---

## Success Criteria

- [ ] Unitários de `application` cobrem envio e ausência de envio quando a regra falha.
- [ ] Integração cobre envio contra SMTP de teste e falha de SMTP sem afetar o status.
- [ ] `docs/ddd/linguagem-ubiqua.md` (Decisões do MVP) e `docs/ddd/modelo-de-dominio.md` atualizados.
