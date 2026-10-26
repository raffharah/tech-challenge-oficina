# Decisão sobre o Orçamento Specification

## Problem Statement

O fluxo de autorizações é ineficiente. Depois do envio, o cliente aprova, recusa parte ou recusa todo o orçamento (fluxo-os 3.2 passos 12-13 e 3.3). A aprovação reserva os itens no estoque e libera a execução; a recusa parcial abre uma única renegociação; a recusa total cancela a OS.

## Goals

- [ ] Registro da aprovação ou rejeição do orçamento (funcionalidade obrigatória).
- [ ] Aprovação com reserva tudo-ou-nada e passagem automática para Em execução.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Autenticação do cliente | AD-001: `/cliente` simula a resposta |
| Aprovação parcial com reserva | AD-003 |
| Expiração de Aguardando aprovação | Decisões do MVP: não expira |
| Devolução do veículo na OS cancelada por rejeição | O fluxo termina em "OS cancelada"; devolução não muda status (glossário) |
| Decisão sobre orçamento de serviço adicional | Escopo `servico-adicional` |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Canal da resposta do cliente | Endpoints `/cliente` sem JWT recebem aprovação, recusa parcial e recusa total; representam a resposta simulada do cliente | Decisão do usuário (AD-001) | y |
| "Reorganiza a OS" | O sistema remove os componentes recusados e volta a OS para EM_DIAGNOSTICO; o funcionário ajusta e reenvia pelo fluxo de orçamento | Achado V10 | n |
| Recusa parcial de todos os componentes | Rejeitada com 422; o cliente deve usar recusa total | Recusa parcial pressupõe algo restante | n |
| Falta de estoque na aprovação | Nada reservado, OS permanece AGUARDANDO_APROVACAO, funcionário notificado com os itens faltantes, resposta 422 com a lista; a aprovação pode ser repetida | Decisões do MVP; achado V2 | n |
| Notificação da execução | Aprovação com reserva leva a OS a EM_EXECUCAO e notifica funcionário (aprovação) e cliente (em execução) | Políticas fluxo-os 3.2 e 4.1 (AD-002) | n |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Aprovar orçamento ⭐ MVP

**User Story**: Como cliente, quero aprovar o orçamento para que o serviço comece.

**Why P1**: Caminho principal até o primeiro Ponto Pivotal.

**Acceptance Criteria**:

1. WHILE a OS está AGUARDANDO_APROVACAO, WHEN o cliente aprova o orçamento vigente THEN o sistema SHALL reservar os itens pela porta `EstoqueDeItens` e mudar a situação do orçamento para APROVADO.
2. WHEN a reserva é concluída THEN o sistema SHALL notificar o funcionário da aprovação, mudar a OS para EM_EXECUCAO e notificar o cliente que a OS está em execução.
3. IF falta saldo disponível para algum item THEN o sistema SHALL manter a OS em AGUARDANDO_APROVACAO e o orçamento ENVIADO, notificar o funcionário com os itens faltantes e responder 422 com a mesma lista.
4. IF a OS não está AGUARDANDO_APROVACAO THEN o sistema SHALL responder 422.

**Independent Test**: Aprovar com saldo (OS EM_EXECUCAO, saldo reservado aumenta) e sem saldo (OS continua AGUARDANDO_APROVACAO, nada reservado).

---

### P1: Recusa total ⭐ MVP

**User Story**: Como cliente, quero recusar todo o orçamento.

**Why P1**: Registro da rejeição é obrigatório.

**Acceptance Criteria**:

1. WHILE a OS está AGUARDANDO_APROVACAO, WHEN o cliente recusa o orçamento totalmente THEN o sistema SHALL mudar o orçamento para REJEITADO e a OS para CANCELADA.
2. WHEN a OS é cancelada por rejeição THEN o sistema SHALL notificar o funcionário sobre a rejeição do orçamento.
3. WHILE a OS está CANCELADA, the sistema SHALL rejeitar qualquer comando que altere a OS com 422.

**Independent Test**: Recusar totalmente e confirmar status CANCELADA e notificação ao funcionário.

---

### P1: Recusa parcial e renegociação ⭐ MVP

**User Story**: Como cliente, quero recusar alguns componentes e receber um novo orçamento.

**Why P1**: Exceção principal da fase 1 do fluxo da OS.

**Acceptance Criteria**:

1. WHILE a OS está AGUARDANDO_APROVACAO com o primeiro orçamento, WHEN o cliente recusa parte dos componentes THEN o sistema SHALL mudar o orçamento para REJEITADO, remover os componentes recusados da OS e mudar a OS para EM_DIAGNOSTICO.
2. WHEN a recusa parcial é registrada THEN o sistema SHALL notificar o funcionário com os componentes recusados.
3. WHEN o orçamento renegociado é enviado THEN o sistema SHALL registrá-lo como nova versão e marcar a OS como em renegociação.
4. IF o cliente pede recusa parcial de orçamento renegociado THEN o sistema SHALL responder 422 (só aprovar ou recusar totalmente).
5. IF a recusa parcial inclui todos os componentes ou componente que não pertence ao orçamento THEN o sistema SHALL responder 422.

**Independent Test**: Recusar um de dois serviços, reenviar, tentar nova recusa parcial (422) e aprovar.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| ORC-01 | P1: Aprovar orçamento | - | Pending |
| ORC-02 | P1: Recusa total | - | Pending |
| ORC-03 | P1: Recusa parcial e renegociação | - | Pending |

**Coverage:** 3 total, 0 mapped tasks, 3 unmapped ⚠️

---

## Success Criteria

- [ ] Teste de integração cobre as três saídas de AGUARDANDO_APROVACAO e a falta de estoque na aprovação.
