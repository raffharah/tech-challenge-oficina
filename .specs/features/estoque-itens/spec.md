# Estoque de Peças e Insumos Specification

## Problem Statement

A oficina falha no controle de peças e insumos. O MVP exige CRUD de peças e insumos com controle de estoque: preço de venda para o orçamento e saldo que baixa quando o orçamento é aprovado.

## Goals

- [ ] CRUD de itens (peça ou insumo) com preço de venda e saldo físico.
- [ ] Porta usada pela OS: consultar preço e dar baixa tudo-ou-nada.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Reserva e saldo reservado | AD-004: baixa direta na aprovação |
| Estoque mínimo e alerta | Sem item obrigatório |
| Descarte, devolução, inativação | Sem item obrigatório |
| Fornecedor, garantia, peça defeituosa | Sem item obrigatório |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Dados do item | SKU (único), nome, tipo PECA ou INSUMO, preço de venda > 0, saldo inicial ≥ 0 | Glossário; requisito cita peças e insumos | n |
| Quantidade | Inteiro positivo | Unidade de medida fora do MVP | n |
| Saldo editável | Só por entrada e baixa; não é digitado na atualização do item | Controle de estoque auditável | n |
| Remoção | Item referenciado por OS não é removido (409) | Preserva histórico | n |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: CRUD de itens ⭐ MVP

**User Story**: Como funcionário, quero manter peças e insumos com preço de venda.

**Why P1**: CRUD obrigatório; preço alimenta o orçamento.

**Acceptance Criteria**:

1. WHEN o funcionário cadastra item com SKU novo THEN o sistema SHALL persistir com saldo físico igual ao saldo inicial e responder 201.
2. IF o SKU já existe THEN o sistema SHALL responder 409.
3. IF o preço de venda é menor ou igual a zero ou o saldo inicial é negativo THEN o sistema SHALL responder 422.
4. WHEN o funcionário atualiza nome ou preço de venda THEN o sistema SHALL persistir sem alterar o saldo.
5. WHEN o funcionário lista ou consulta itens THEN o sistema SHALL retornar tipo, preço de venda e saldo físico.
6. IF o funcionário remove item referenciado por OS THEN o sistema SHALL responder 409.

**Independent Test**: SKU novo (201) e repetido (409).

---

### P1: Entrada de estoque ⭐ MVP

**User Story**: Como funcionário, quero registrar entradas para repor o saldo.

**Why P1**: Controle de estoque.

**Acceptance Criteria**:

1. WHEN o funcionário registra entrada de quantidade positiva THEN o sistema SHALL somá-la ao saldo físico.
2. IF a quantidade é menor ou igual a zero THEN o sistema SHALL responder 422.

**Independent Test**: Saldo 10, entrada 5, saldo 15.

---

### P1: Baixa de estoque ⭐ MVP

**User Story**: Como OS aprovada, quero baixar do estoque os itens do orçamento.

**Why P1**: Controle de estoque ligado à OS (AD-004).

**Acceptance Criteria**:

1. WHEN a baixa de uma lista de itens é pedida e todos têm saldo físico suficiente THEN o sistema SHALL subtrair cada quantidade do saldo físico.
2. IF algum item não tem saldo físico suficiente THEN o sistema SHALL não baixar nenhum item e lançar `DomainException` com SKU, quantidade pedida e saldo físico de cada item faltante.
3. The sistema SHALL nunca deixar saldo físico negativo.

**Independent Test**: Saldo 1, duas baixas de 1: a segunda falha e o saldo fica 0.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| EST-01 | P1: CRUD de itens | - | Pending |
| EST-02 | P1: Entrada de estoque | - | Pending |
| EST-03 | P1: Baixa de estoque | - | Pending |

**Coverage:** 3 total, 0 mapped tasks, 3 unmapped ⚠️

---

## Success Criteria

- [ ] Duas aprovações concorrentes pelo último item: só uma baixa (teste de integração).
