# Execução dos Serviços Specification

## Problem Statement

Não há como acompanhar o andamento dos serviços. Com a OS Em execução, o mecânico inicia e conclui cada serviço; ao concluir o último, a OS é finalizada, o estoque consome os itens reservados e o cliente é avisado (fluxo-os 4.1; estoque 7.1). Serviços que passam do prazo precisam ser comunicados.

## Goals

- [ ] Início e conclusão de cada serviço com data/hora (base do tempo médio de execução).
- [ ] Finalização automática da OS com consumo de estoque.
- [ ] Rotina diária de atraso.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Problema no veículo e serviço adicional | Escopo `servico-adicional` |
| Cobrança após a finalização | Escopo `cobranca-entrega` |
| Pausa de serviço | Não aparece nos fluxos |
| Atraso por serviço individual | AD-004 |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Ordem dos serviços | Serviços podem ser iniciados em qualquer ordem e em paralelo | Fluxo não impõe ordem | n |
| Finalização da OS | Automática ao concluir o último serviço não concluído | Política "Atualizar o estado da OS para finalizada" (AD-002) | n |
| Consumo | Na finalização, pela porta `EstoqueDeItens`, consome toda a Reserva da OS | Glossário "Consumo" | n |
| Atraso | Rotina diária marca a OS atrasada quando a data atual é posterior ao prazo e a OS não está FINALIZADA, ENTREGUE ou CANCELADA; notifica o cliente uma vez por prazo | AD-004 | n |
| Nova data | Funcionário atualiza o prazo; o cliente é notificado com a nova data e a marca de atraso é removida | Evento "Cliente foi notificado com nova data" | n |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Iniciar e concluir serviços ⭐ MVP

**User Story**: Como mecânico, quero registrar o início e a conclusão de cada serviço da OS.

**Why P1**: Caminho principal da fase Serviço.

**Acceptance Criteria**:

1. WHILE a OS está EM_EXECUCAO, WHEN o mecânico inicia um serviço não iniciado THEN o sistema SHALL registrar a data/hora de início.
2. WHILE a OS está EM_EXECUCAO, WHEN o mecânico conclui um serviço iniciado THEN o sistema SHALL registrar a data/hora de conclusão.
3. IF o serviço já foi iniciado e é iniciado de novo, ou é concluído sem ter sido iniciado, ou já está concluído THEN o sistema SHALL responder 422.
4. IF a OS não está EM_EXECUCAO THEN o sistema SHALL rejeitar início e conclusão com 422.

**Independent Test**: Iniciar e concluir um serviço; tentar concluir sem iniciar (422).

---

### P1: Finalizar OS ⭐ MVP

**User Story**: Como sistema, quero finalizar a OS quando todos os serviços terminam.

**Why P1**: Segundo Ponto Pivotal; libera a cobrança.

**Acceptance Criteria**:

1. WHEN o último serviço não concluído da OS é concluído THEN o sistema SHALL mudar a OS para FINALIZADA e registrar a data/hora da finalização.
2. WHEN a OS é finalizada THEN o sistema SHALL consumir a Reserva da OS pela porta `EstoqueDeItens`.
3. WHEN a OS é finalizada THEN o sistema SHALL notificar o cliente que a OS está finalizada.
4. WHILE algum serviço da OS não está concluído, the sistema SHALL manter a OS EM_EXECUCAO.

**Independent Test**: OS com dois serviços: concluir o primeiro mantém EM_EXECUCAO; concluir o segundo finaliza e baixa o saldo físico.

---

### P2: Atraso da OS

**User Story**: Como cliente, quero ser avisado quando o prazo do orçamento passa.

**Why P2**: Desvio "serviço atrasado" da fase Serviço.

**Acceptance Criteria**:

1. WHEN a rotina diária roda e a data atual é posterior ao prazo do orçamento vigente de uma OS não FINALIZADA, ENTREGUE ou CANCELADA THEN o sistema SHALL marcar a OS como atrasada e notificar o cliente.
2. IF a OS já está marcada como atrasada para o mesmo prazo THEN o sistema SHALL não enviar nova notificação.
3. WHEN o funcionário informa um novo prazo posterior à data atual para uma OS EM_EXECUCAO THEN o sistema SHALL atualizar o prazo, remover a marca de atraso e notificar o cliente com a nova data.
4. The sistema SHALL obter a data atual por `Clock` injetado.

**Independent Test**: `Clock` fixo um dia após o prazo: rotina marca e notifica uma vez; segunda execução não notifica.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| EXE-01 | P1: Iniciar e concluir serviços | - | Pending |
| EXE-02 | P1: Finalizar OS | - | Pending |
| EXE-03 | P2: Atraso da OS | - | Pending |

**Coverage:** 3 total, 0 mapped tasks, 3 unmapped ⚠️

---

## Success Criteria

- [ ] Saldo físico e reservado corretos após a finalização (teste de integração OS + estoque).
