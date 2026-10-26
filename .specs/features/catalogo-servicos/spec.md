# Catálogo de Serviços Specification

## Problem Statement

O orçamento soma o valor padrão dos serviços (fluxo-os 3.2, passo 9) e o funcionário escolhe serviços da "Lista de serviços disponíveis". Não existe catálogo nem valor padrão cadastrado.

## Goals

- [ ] CRUD de serviços com valor padrão, consumido pela abertura da OS.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Peças e insumos necessários por serviço (kit) | ML "Lista de serviços com peças e insumos" fica como escolha manual do funcionário |
| Tempo estimado por serviço | Prazo é informado manualmente no orçamento (Decisões do MVP) |
| Histórico de preços | Valor é copiado para a OS no momento da inclusão |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Dados do serviço | Nome (único), descrição opcional, valor padrão > 0, situação ATIVO/INATIVO | Mínimo para o orçamento | n |
| Remoção | Serviço já usado em OS não é removido (409); é inativado | Preserva histórico das OS | n |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Manter catálogo de serviços ⭐ MVP

**User Story**: Como funcionário, quero cadastrar os serviços da oficina com valor padrão para montar orçamentos.

**Why P1**: CRUD obrigatório e base do orçamento.

**Acceptance Criteria**:

1. WHEN o funcionário cadastra um serviço com nome e valor padrão maior que zero THEN o sistema SHALL persistir com situação ATIVO e responder 201.
2. IF o valor padrão é menor ou igual a zero THEN o sistema SHALL responder 422.
3. IF já existe serviço com o mesmo nome, sem diferenciar maiúsculas THEN o sistema SHALL responder 409.
4. WHEN o funcionário atualiza nome, descrição ou valor padrão THEN o sistema SHALL persistir sem alterar valores já copiados para OS existentes.
5. WHEN o funcionário inativa um serviço THEN o sistema SHALL mantê-lo nas consultas com situação INATIVO.
6. WHEN o funcionário lista serviços filtrando por situação ATIVO THEN o sistema SHALL retornar só os ativos.

**Independent Test**: Cadastrar, alterar valor e listar ativos.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| SRV-01 | P1: Manter catálogo de serviços | - | Pending |

**Coverage:** 1 total, 0 mapped tasks, 1 unmapped ⚠️

---

## Success Criteria

- [ ] Serviços ativos aparecem na listagem usada para montar o orçamento.
