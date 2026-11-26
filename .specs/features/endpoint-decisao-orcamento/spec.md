# Endpoint Único de Decisão do Orçamento Specification

## Problem Statement

A fase 2 pede um endpoint para receber notificações externas de aprovação ou recusa do orçamento do cliente (`docs/fase2-tech-challenge.md:29`). Hoje a decisão chega por duas rotas, `POST /cliente/ordens-servico/{id}/aprovacao` e `/rejeicao`. As duas passam a ser um endpoint só, com a decisão no corpo, mantendo a identificação pelo CPF/CNPJ do titular (AD-001).

## Goals

- [x] DEC-01: um endpoint recebe a aprovação ou a recusa do orçamento.
- [x] DEC-02: as rotas `/aprovacao` e `/rejeicao` deixam de existir.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Webhook com token de serviço externo | Não está na documentação da fase 2; o cliente segue identificado pelo CPF/CNPJ (AD-001) |
| Recusa parcial e renegociação | AD-003 |
| Mudança na baixa de estoque | AD-004: tudo ou nada na aprovação |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Formato do endpoint | `POST /cliente/ordens-servico/{id}/decisao` com `{"cpfCnpj": "...", "decisao": "APROVADO" \| "RECUSADO"}` | Opção b escolhida pelo usuário | y |
| Rotas antigas | Removidas | Não manter dois caminhos para a mesma decisão | y |
| Efeito de `RECUSADO` | OS vai para `CANCELADA`, como a rejeição atual | Reaproveita `RejeitarOrcamento` | y |
| Decisão repetida | `422 TRANSICAO_INVALIDA`, como hoje | Sem requisito de reenvio | y |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Decidir o orçamento por um endpoint ⭐ MVP

**User Story**: Como cliente, quero aprovar ou recusar o orçamento por um único endpoint.

**Why P1**: Requisito obrigatório da fase 2.

**Acceptance Criteria**:

1. WHILE a OS está `AGUARDANDO_APROVACAO`, WHEN chega `decisao` `APROVADO` com o CPF/CNPJ do titular THEN o sistema SHALL baixar os itens pela porta `EstoqueDeItens`, mudar a OS para `EM_EXECUCAO` e responder 200 com a OS.
2. WHILE a OS está `AGUARDANDO_APROVACAO`, WHEN chega `decisao` `RECUSADO` com o CPF/CNPJ do titular THEN o sistema SHALL mudar a OS para `CANCELADA` sem baixar itens e responder 200 com a OS.
3. IF falta saldo para algum item na aprovação THEN o sistema SHALL responder 422 `SALDO_INSUFICIENTE` com os itens em falta e manter a OS `AGUARDANDO_APROVACAO`, sem baixar nenhum item.
4. IF a OS não está `AGUARDANDO_APROVACAO` THEN o sistema SHALL responder 422 `TRANSICAO_INVALIDA`.
5. IF a OS não existe ou o CPF/CNPJ não é do titular THEN o sistema SHALL responder 404 `ORDEM_SERVICO_NAO_ENCONTRADA` nos dois casos, sem mudar a OS.
6. IF `cpfCnpj` ou `decisao` falta, ou `decisao` não é `APROVADO` nem `RECUSADO`, THEN o sistema SHALL responder 400.
7. IF o `cpfCnpj` é inválido THEN o sistema SHALL responder 422 `CPF_CNPJ_INVALIDO`.

**Independent Test**: Aprovar com saldo, aprovar sem saldo, recusar e decidir com CPF/CNPJ de outro titular.

---

### P1: Remover as rotas antigas ⭐ MVP

**User Story**: Como oficina, quero um único caminho para a decisão do orçamento.

**Why P1**: Duas formas de decidir duplicam contrato e documentação.

**Acceptance Criteria**:

1. WHEN um chamador envia `POST /cliente/ordens-servico/{id}/aprovacao` ou `/rejeicao` THEN o sistema SHALL responder 404 sem alterar a OS.
2. The sistema SHALL manter `GET /cliente/ordens-servico` e `GET /cliente/ordens-servico/{id}` sem mudança.

**Independent Test**: Chamar as rotas antigas e conferir que a OS segue `AGUARDANDO_APROVACAO`.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| DEC-01 | P1: Decidir o orçamento por um endpoint | Tasks | Verified |
| DEC-02 | P1: Remover as rotas antigas | Tasks | Verified |

**Coverage:** 2 total, 2 mapped to tasks, 0 unmapped

---

## Success Criteria

- [x] `DecisaoOrcamentoIT` cobre aprovação, recusa, falta de saldo, outro titular e rotas antigas removidas.
