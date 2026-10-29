# Execução e Entrega Specification

## Problem Statement

Os status Finalizada e Entregue são obrigatórios. Com a OS Em execução, o mecânico registra o fim dos serviços e o funcionário registra a entrega do veículo.

## Goals

- [x] OS percorre Em execução → Finalizada → Entregue com data/hora no histórico (base do tempo médio, AD-005).

## Out of Scope

| Feature | Reason |
| --- | --- |
| Início e conclusão por serviço | AD-005 |
| Prazo e atraso | AD-003 |
| Serviço adicional | AD-003 |
| Cobrança e pagamento antes da entrega | AD-003 |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Finalização | Comando do mecânico finaliza a OS inteira | Sem controle por serviço (AD-005) | y |
| Entrega | Comando do funcionário, sem pré-condição de pagamento | Pagamento fora do MVP | y |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Finalizar OS ⭐ MVP

**User Story**: Como mecânico, quero finalizar a OS quando os serviços terminam.

**Why P1**: Status Finalizada obrigatório.

**Acceptance Criteria**:

1. WHILE a OS está EM_EXECUCAO, WHEN o mecânico finaliza THEN o sistema SHALL mudar a OS para FINALIZADA e registrar data/hora.
2. IF a OS não está EM_EXECUCAO THEN o sistema SHALL responder 422.

**Independent Test**: Finalizar OS em execução (FINALIZADA) e em diagnóstico (422).

---

### P1: Entregar veículo ⭐ MVP

**User Story**: Como funcionário, quero registrar a entrega do veículo.

**Why P1**: Status Entregue obrigatório.

**Acceptance Criteria**:

1. WHILE a OS está FINALIZADA, WHEN o funcionário registra a entrega THEN o sistema SHALL mudar a OS para ENTREGUE e registrar data/hora.
2. IF a OS não está FINALIZADA THEN o sistema SHALL responder 422.

**Independent Test**: Entregar OS finalizada (ENTREGUE) e em execução (422).

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| EXE-01 | P1: Finalizar OS | Tasks | Verified |
| EXE-02 | P1: Entregar veículo | Tasks | Verified |

**Coverage:** 2 total, 2 mapped tasks, 0 unmapped

---

## Success Criteria

- [x] Fluxo completo abertura → entrega demonstrável pelo Swagger.
