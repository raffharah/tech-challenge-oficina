# Listagem Priorizada de OS Specification

## Problem Statement

A fase 2 exige que a listagem de OS da oficina mostre primeiro o que está em andamento, por prioridade de status e das mais antigas para as mais novas, sem as OS finalizadas e entregues. Hoje `GET /ordens-servico` lista todas as OS da mais recente para a mais antiga (`OrdemServicoRepositoryImpl.maisNovasPrimeiro`).

## Goals

- [x] LST-01: listagem ordenada por prioridade de status.
- [x] LST-02: dentro do mesmo status, a OS mais antiga vem primeiro.
- [x] LST-03: OS finalizadas e entregues ficam fora da listagem, sem remoção do banco.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Listagem do cliente (`GET /cliente/ordens-servico`) | O requisito trata da listagem da oficina; a do cliente mostra todo o histórico do titular |
| Filtro por status na requisição | Sem requisito |
| Exclusão física ou flag `excluida` na OS | O requisito pede exclusão lógica da listagem; o status já decide quem aparece |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| OS `CANCELADA` | Continua na listagem | Decisão do usuário: removê-la seria regressão da fase 1 | y |
| Posição de `CANCELADA` na ordem | Última, depois de `RECEBIDA` | O requisito não a cita; status final sem ação pendente fica no fim | n |
| Desempate de mesma data de abertura | `id` crescente | Mantém a ordem estável entre páginas | y |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Listar OS em andamento por prioridade ⭐ MVP

**User Story**: Como funcionário, quero ver as OS em andamento por prioridade de status e por antiguidade para atender primeiro o que está mais adiantado e mais atrasado.

**Why P1**: Requisito obrigatório da fase 2.

**Acceptance Criteria**:

1. WHEN o funcionário lista as OS THEN o sistema SHALL ordenar por status na sequência `EM_EXECUCAO`, `AGUARDANDO_APROVACAO`, `EM_DIAGNOSTICO`, `RECEBIDA`, `CANCELADA`.
2. WHEN duas OS têm o mesmo status THEN o sistema SHALL listar primeiro a de `abertaEm` menor e, com `abertaEm` igual, a de `id` menor.
3. WHEN o funcionário lista as OS THEN o sistema SHALL omitir as OS com status `FINALIZADA` ou `ENTREGUE`.
4. The sistema SHALL manter as OS `FINALIZADA` e `ENTREGUE` acessíveis por `GET /ordens-servico/{id}` e no tempo médio de execução.
5. The sistema SHALL calcular `totalElementos` da página só com as OS listadas.

**Independent Test**: Criar OS em todos os status com datas variadas; a listagem devolve a ordem esperada, sem `FINALIZADA` e `ENTREGUE`, e o detalhe de uma OS `ENTREGUE` continua respondendo 200.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| LST-01 | P1: Listar OS em andamento por prioridade | - | Done |
| LST-02 | P1: Listar OS em andamento por prioridade | - | Done |
| LST-03 | P1: Listar OS em andamento por prioridade | - | Done |

**Coverage:** 3 total, 3 mapped to tasks, 0 unmapped

---

## Success Criteria

- [ ] Integração com PostgreSQL cobre ordem por status, ordem por data e exclusão de `FINALIZADA`/`ENTREGUE` com paginação.
