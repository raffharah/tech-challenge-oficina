# Inadimplência Specification

## Problem Statement

Quando o cliente não paga, a OS finalizada precisa evoluir para atraso, vencimento e cancelamento, suspendendo o cliente, devolvendo peças recuperadas ao estoque e devolvendo o veículo (fluxo-os 5.2; estoque 7.2). Os prazos são de 5 dias úteis.

## Goals

- [ ] Rotina diária move Pagamento A_VENCER → EM_ATRASO → VENCIDO com notificações.
- [ ] Vencimento cancela a OS e suspende o cliente; peças recuperadas voltam ao estoque (AD-007).

## Out of Scope

| Feature | Reason |
| --- | --- |
| Calendário de feriados | AD-005 |
| Multa e juros | Não aparecem nos fluxos |
| Negociação de dívida | Não aparece nos fluxos |
| Reativação do cliente | Manual, escopo `clientes-veiculos` |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Contagem | Dias úteis (seg-sex) a partir da data da finalização para atraso, e da data da notificação de atraso para vencimento; atinge o limite no 5º dia útil completo | Glossário; AD-005; achado V12 | n |
| Rotina | Job diário com `Clock` injetado; idempotente (reexecução no mesmo dia não repete transição nem notificação) | Decisões do MVP: rotina agendada | n |
| Cancelamento | Cancelamento da OS, suspensão do cliente e notificação aos funcionários na mesma transação do vencimento | Políticas encadeadas (AD-002) | n |
| Desfazer serviço | Funcionário informa as peças recuperadas e quantidades; o restante das peças consumidas é registrado como perda ou avaria; insumos não voltam | AD-007 | n |
| Retirada após cancelamento | Após desfazer o serviço, cliente recebe notificação de retirada; a entrega registra devolução do veículo e mantém CANCELADA | Glossário "Devolução do veículo" | y |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Pagamento em atraso ⭐ MVP

**User Story**: Como oficina, quero identificar e avisar pagamentos em atraso.

**Why P1**: Primeira etapa do desvio 5.2.

**Acceptance Criteria**:

1. WHEN a rotina diária roda e um Pagamento A_VENCER completou 5 dias úteis desde a finalização da OS THEN o sistema SHALL mudar o Pagamento para EM_ATRASO e registrar a data da notificação de atraso.
2. WHEN o Pagamento muda para EM_ATRASO THEN o sistema SHALL notificar o cliente e os funcionários do atraso.
3. IF a rotina roda de novo no mesmo dia THEN o sistema SHALL não repetir a transição nem a notificação.

**Independent Test**: Finalização numa segunda; `Clock` na segunda seguinte: EM_ATRASO; na sexta anterior: A_VENCER.

---

### P1: Vencimento e cancelamento ⭐ MVP

**User Story**: Como oficina, quero cancelar a OS e suspender o cliente quando o pagamento vence.

**Why P1**: Cancelamento por inadimplência (glossário).

**Acceptance Criteria**:

1. WHEN a rotina diária roda e um Pagamento EM_ATRASO completou 5 dias úteis desde a notificação de atraso THEN o sistema SHALL mudar o Pagamento para VENCIDO.
2. WHEN o Pagamento vence THEN o sistema SHALL mudar a OS de FINALIZADA para CANCELADA, suspender o cliente e notificar os funcionários do cancelamento por inadimplência.
3. WHILE o cliente está SUSPENSO, the sistema SHALL rejeitar a abertura de nova OS para ele (regra OS-01).

**Independent Test**: `Clock` 10 dias úteis após a finalização: Pagamento VENCIDO, OS CANCELADA, cliente SUSPENSO.

---

### P1: Desfazer serviço e devolver peças ⭐ MVP

**User Story**: Como funcionário, quero desfazer o serviço da OS cancelada e devolver as peças recuperadas ao estoque.

**Why P1**: Fecha o desvio 5.2 e a trilha 7.2 do estoque.

**Acceptance Criteria**:

1. WHILE a OS está CANCELADA por inadimplência, WHEN o funcionário desfaz o serviço informando peças recuperadas THEN o sistema SHALL devolvê-las pela porta `EstoqueDeItens` e registrar o restante das peças consumidas como perda ou avaria.
2. WHEN o serviço é desfeito THEN o sistema SHALL enviar ao cliente a notificação de retirada do veículo.
3. WHEN o funcionário registra a entrega de OS CANCELADA com serviço desfeito THEN o sistema SHALL registrar a devolução do veículo e manter a OS CANCELADA.
4. IF a quantidade recuperada de uma peça excede a consumida pela OS THEN o sistema SHALL responder 422 sem devolver nada.
5. IF o serviço da OS já foi desfeito THEN o sistema SHALL responder 422.

**Independent Test**: OS consumiu 2 pastilhas; recuperar 1: saldo físico +1 e perda de 1 registrada.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| INA-01 | P1: Pagamento em atraso | - | Pending |
| INA-02 | P1: Vencimento e cancelamento | - | Pending |
| INA-03 | P1: Desfazer serviço e devolver peças | - | Pending |

**Coverage:** 3 total, 0 mapped tasks, 3 unmapped ⚠️

---

## Success Criteria

- [ ] Testes de domínio cobrem a contagem de dias úteis atravessando fins de semana.
