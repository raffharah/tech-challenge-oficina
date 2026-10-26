# Decisão sobre o Orçamento Specification

## Problem Statement

O MVP exige registrar a aprovação ou a rejeição do orçamento. A aprovação baixa os itens do estoque e libera a execução; a rejeição encerra a OS.

## Goals

- [ ] Aprovação e rejeição registradas pela resposta simulada do cliente.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Recusa parcial e renegociação | AD-003 |
| Autenticação do cliente | AD-001 |
| Notificações ao funcionário e ao cliente | AD-003 |
| Expiração de Aguardando aprovação | Sem item obrigatório |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Canal da resposta | Endpoints `/cliente` sem JWT para aprovar e rejeitar | AD-001, decisão do usuário | y |
| Falta de estoque na aprovação | Nada é baixado, OS continua AGUARDANDO_APROVACAO, resposta 422 com os itens faltantes; aprovação pode ser repetida após entrada | AD-004 | n |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Aprovar orçamento ⭐ MVP

**User Story**: Como cliente, quero aprovar o orçamento para o serviço começar.

**Why P1**: Registro da aprovação é obrigatório.

**Acceptance Criteria**:

1. WHILE a OS está AGUARDANDO_APROVACAO, WHEN o cliente aprova THEN o sistema SHALL baixar os itens da OS pela porta `EstoqueDeItens` e mudar a OS para EM_EXECUCAO na mesma transação.
2. IF falta saldo para algum item THEN o sistema SHALL manter a OS AGUARDANDO_APROVACAO, não baixar nenhum item e responder 422 com os itens faltantes.
3. IF a OS não está AGUARDANDO_APROVACAO THEN o sistema SHALL responder 422.

**Independent Test**: Aprovar com saldo (EM_EXECUCAO, saldo baixado) e sem saldo (AGUARDANDO_APROVACAO, saldo intacto).

---

### P1: Rejeitar orçamento ⭐ MVP

**User Story**: Como cliente, quero rejeitar o orçamento.

**Why P1**: Registro da rejeição é obrigatório.

**Acceptance Criteria**:

1. WHILE a OS está AGUARDANDO_APROVACAO, WHEN o cliente rejeita THEN o sistema SHALL mudar a OS para CANCELADA.
2. WHILE a OS está CANCELADA, the sistema SHALL rejeitar qualquer comando que altere a OS com 422.

**Independent Test**: Rejeitar e tentar incluir serviço (422).

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| ORC-01 | P1: Aprovar orçamento | - | Pending |
| ORC-02 | P1: Rejeitar orçamento | - | Pending |

**Coverage:** 2 total, 0 mapped tasks, 2 unmapped ⚠️

---

## Success Criteria

- [ ] Integração cobre aprovação com saldo, aprovação sem saldo e rejeição.
