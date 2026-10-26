# Acompanhamento da OS Specification

## Problem Statement

Clientes e funcionários não conseguem acompanhar o status dos serviços. O desafio exige consulta do andamento pelo cliente via API, listagem e detalhamento de OS na API administrativa e monitoramento do tempo médio de execução dos serviços.

## Goals

- [ ] Cliente consulta o andamento da própria OS.
- [ ] Funcionário lista, filtra e detalha OS e consulta o tempo médio de execução.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Priorização da listagem por status | Não exigido na fase 1 |
| Tempo real (push, WebSocket) | Consulta por API atende o requisito |
| Tempo médio por mecânico | Não há cadastro de funcionários |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Acesso do cliente | `/cliente` sem JWT; exige número da OS e CPF/CNPJ do titular | AD-001; evita expor OS por enumeração | n |
| Conteúdo para o cliente | Status atual, histórico de status com data/hora, orçamento vigente (componentes, total, prazo), marca de atraso e situação do pagamento | Requisito "acompanhar o progresso" | n |
| Tempo de execução da OS | Do primeiro início de serviço até a última conclusão, em minutos | Glossário "Tempo médio de execução" | n |
| Tempo médio | Média das OS que chegaram a FINALIZADA, filtrável por período da finalização; também por serviço do catálogo (início → conclusão de cada serviço) | Glossário; requisito "monitoramento" | n |
| Ordenação da listagem | Data de abertura decrescente, paginada | Padrão neutro | n |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Consulta do cliente ⭐ MVP

**User Story**: Como cliente, quero consultar o andamento da minha OS.

**Why P1**: Funcionalidade obrigatória.

**Acceptance Criteria**:

1. WHEN o cliente consulta informando número da OS e CPF/CNPJ do titular THEN o sistema SHALL retornar status atual, histórico de status com data/hora, orçamento vigente, prazo, marca de atraso e situação do pagamento.
2. IF a OS não existe ou o CPF/CNPJ não é do titular THEN o sistema SHALL responder 404 com a mesma mensagem nos dois casos.

**Independent Test**: Consultar com CPF correto (200) e com CPF de outro cliente (404).

---

### P1: Listagem e detalhamento ⭐ MVP

**User Story**: Como funcionário, quero listar e detalhar as OS.

**Why P1**: Funcionalidade obrigatória da gestão administrativa.

**Acceptance Criteria**:

1. WHEN o funcionário lista OS THEN o sistema SHALL retornar página ordenada por data de abertura decrescente com número, cliente, placa, status e total vigente.
2. WHEN o funcionário filtra por status ou por CPF/CNPJ do cliente THEN o sistema SHALL retornar só as OS que atendem ao filtro.
3. WHEN o funcionário detalha uma OS THEN o sistema SHALL retornar cliente, veículo, componentes com disponibilidade, histórico de status, histórico de orçamentos, datas de início e conclusão dos serviços, pagamento, nota fiscal e notificações.
4. IF a OS não existe THEN o sistema SHALL responder 404.

**Independent Test**: Duas OS em status diferentes; filtrar por um status retorna uma.

---

### P2: Tempo médio de execução

**User Story**: Como gestor, quero saber o tempo médio de execução dos serviços.

**Why P2**: Requisito obrigatório, mas depende de OS finalizadas.

**Acceptance Criteria**:

1. WHEN o funcionário consulta o tempo médio THEN o sistema SHALL retornar a média, em minutos, do intervalo entre o primeiro início e a última conclusão de serviços das OS que chegaram a FINALIZADA, e a quantidade de OS consideradas.
2. WHEN um período é informado THEN o sistema SHALL considerar só OS finalizadas dentro do período.
3. WHEN o funcionário consulta o tempo médio por serviço THEN o sistema SHALL retornar, para cada serviço do catálogo, a média do intervalo início → conclusão.
4. IF nenhuma OS atende ao filtro THEN o sistema SHALL retornar quantidade zero e média nula.

**Independent Test**: Duas OS finalizadas com 60 e 120 minutos: média 90, quantidade 2.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| ACO-01 | P1: Consulta do cliente | - | Pending |
| ACO-02 | P1: Listagem e detalhamento | - | Pending |
| ACO-03 | P2: Tempo médio de execução | - | Pending |

**Coverage:** 3 total, 0 mapped tasks, 3 unmapped ⚠️

---

## Success Criteria

- [ ] Cliente acompanha a OS do início à entrega só pelo endpoint de consulta.
