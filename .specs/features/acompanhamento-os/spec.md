# Acompanhamento da OS Specification

## Problem Statement

Clientes e funcionários não acompanham o status dos serviços. O MVP exige consulta do andamento pelo cliente via API, listagem e detalhamento de OS e o tempo médio de execução dos serviços.

## Goals

- [ ] Cliente consulta a própria OS, incluindo o orçamento a aprovar.
- [ ] Funcionário lista e detalha OS e consulta o tempo médio.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Filtros na listagem | Sem item obrigatório |
| Tempo médio por serviço ou por período | AD-005; um valor atende "monitoramento ou cálculo" |
| Atualização em tempo real (push) | Consulta por API atende |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Acesso do cliente | `/cliente` sem JWT; exige número da OS e CPF/CNPJ do titular | AD-001 | n |
| Disponibilização do orçamento | A consulta do cliente retorna componentes e total | Atende "envio ou disponibilização" sem e-mail | n |
| Ordenação da listagem | Data de abertura decrescente, paginada | Padrão neutro | n |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Consulta do cliente ⭐ MVP

**User Story**: Como cliente, quero consultar o andamento e o orçamento da minha OS.

**Why P1**: Obrigatório; também disponibiliza o orçamento.

**Acceptance Criteria**:

1. WHEN o cliente consulta com número da OS e CPF/CNPJ do titular THEN o sistema SHALL retornar status atual, histórico de status com data/hora, componentes e total do orçamento.
2. IF a OS não existe ou o CPF/CNPJ não é do titular THEN o sistema SHALL responder 404 com a mesma mensagem nos dois casos.

**Independent Test**: CPF correto (200) e CPF de outro cliente (404).

---

### P1: Listagem e detalhamento ⭐ MVP

**User Story**: Como funcionário, quero listar e detalhar as OS.

**Why P1**: Obrigatório na gestão administrativa.

**Acceptance Criteria**:

1. WHEN o funcionário lista OS THEN o sistema SHALL retornar página ordenada por data de abertura decrescente com número, cliente, placa, status e total.
2. WHEN o funcionário detalha uma OS THEN o sistema SHALL retornar cliente, veículo, componentes, total e histórico de status.
3. IF a OS não existe THEN o sistema SHALL responder 404.

**Independent Test**: Duas OS listadas na ordem correta; detalhe de uma.

---

### P1: Tempo médio de execução ⭐ MVP

**User Story**: Como gestor, quero saber o tempo médio de execução dos serviços.

**Why P1**: Obrigatório na gestão administrativa.

**Acceptance Criteria**:

1. WHEN o funcionário consulta o tempo médio THEN o sistema SHALL retornar a média, em minutos, de (data/hora FINALIZADA − data/hora EM_EXECUCAO) das OS que chegaram a FINALIZADA e a quantidade de OS consideradas.
2. IF nenhuma OS chegou a FINALIZADA THEN o sistema SHALL retornar quantidade zero e média nula.

**Independent Test**: Duas OS com 60 e 120 minutos: média 90, quantidade 2.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| ACO-01 | P1: Consulta do cliente | - | Pending |
| ACO-02 | P1: Listagem e detalhamento | - | Pending |
| ACO-03 | P1: Tempo médio de execução | - | Pending |

**Coverage:** 3 total, 0 mapped tasks, 3 unmapped ⚠️

---

## Success Criteria

- [ ] Cliente acompanha a OS da abertura à entrega só pela consulta.
