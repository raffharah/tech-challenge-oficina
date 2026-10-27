# Catálogo de Serviços Specification

## Problem Statement

O MVP exige CRUD de serviços, e o orçamento automático soma o valor dos serviços incluídos na OS. Não existe catálogo com valor cadastrado.

## Goals

- [ ] CRUD de serviços com valor padrão, usado pela OS.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Peças e insumos sugeridos por serviço | Escolha manual do mecânico |
| Situação ativo/inativo | Remoção bloqueada quando em uso basta |
| Tempo estimado | Sem item obrigatório |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Dados do serviço | Nome (único) e valor padrão > 0 | Mínimo para o orçamento | y |
| Remoção | Serviço referenciado por OS não é removido (409) | Preserva histórico | y |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: CRUD de serviços ⭐ MVP

**User Story**: Como funcionário, quero manter os serviços da oficina com valor padrão.

**Why P1**: CRUD obrigatório e base do orçamento.

**Acceptance Criteria**:

1. WHEN o funcionário cadastra serviço com nome e valor padrão maior que zero THEN o sistema SHALL persistir e responder 201.
2. IF o valor padrão é menor ou igual a zero THEN o sistema SHALL responder 422.
3. IF já existe serviço com o mesmo nome, sem diferenciar maiúsculas THEN o sistema SHALL responder 409.
4. WHEN o funcionário altera nome ou valor padrão THEN o sistema SHALL persistir sem alterar valores já copiados para OS.
5. WHEN o funcionário lista serviços THEN o sistema SHALL retornar lista paginada.
6. IF o funcionário remove serviço referenciado por OS THEN o sistema SHALL responder 409.

**Independent Test**: Cadastrar, alterar valor, listar, remover.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| SRV-01 | P1: CRUD de serviços | Tasks | In Tasks |

**Coverage:** 1 total, 1 mapped tasks, 0 unmapped

---

## Success Criteria

- [ ] Serviços cadastrados aparecem para inclusão na OS.
