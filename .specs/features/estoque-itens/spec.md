# Estoque de Peças e Insumos Specification

## Problem Statement

A oficina falha no controle de peças e insumos. A OS precisa consultar preço e disponibilidade, reservar itens na aprovação e consumir na finalização (fluxo-pecas-insumos fases 1, 3, 4 e 5). Sem saldo físico, reservado e disponível corretos, o orçamento e a execução quebram.

## Goals

- [ ] Item de Estoque com saldo físico, saldo reservado, saldo disponível (nunca negativo), estoque mínimo e situação.
- [ ] Operações que a OS usa por porta: consultar disponibilidade e preço, reservar, consumir e devolver.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Fornecedor: cotação, pedido, reposição, comparação de preço | Decisões do MVP (estoque 5.2, 6.2) |
| Item fora de linha detectado pelo fornecedor | Depende do fornecedor; inativação fica manual |
| Peça defeituosa e garantia | Decisões do MVP (estoque 7.3) |
| Marcação prévia de insumo para descarte | Achado V5: descarte é um único comando |
| Validade/lote de insumo | Comprometimento é avaliado pelo funcionário |
| Liberação de reserva sem consumo | Nenhum fluxo cancela OS com reserva ativa (cancelamentos ocorrem em Aguardando aprovação, sem reserva, ou em Finalizada, já consumida) |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Dados do item | SKU (único), nome, tipo PECA ou INSUMO, preço de venda > 0, estoque mínimo ≥ 0, saldo inicial ≥ 0 | Glossário e fluxo de estoque | n |
| Quantidade | Inteiro positivo, na unidade do item | Simplicidade; unidade de medida fora do MVP | n |
| Saldos editáveis | Saldo físico muda só por entrada, descarte, consumo e devolução; saldo reservado só por reserva e consumo | Saldo não é digitado (glossário) | n |
| Reserva | Tudo ou nada por pedido; cada pedido de reserva acumula na Reserva da OS | Serviço adicional reserva o acréscimo (AD-006) | n |
| Disponibilidade na inclusão | Consulta informativa, sem bloquear (AD-008) | Decisão do usuário | y |
| Alerta de estoque mínimo | Notificação ao funcionário quando saldo físico fica abaixo do mínimo após consumo ou descarte | Glossário; achado V4 | n |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Cadastro de itens ⭐ MVP

**User Story**: Como funcionário, quero cadastrar peças e insumos com preço de venda e estoque mínimo.

**Why P1**: CRUD obrigatório; preço alimenta o orçamento.

**Acceptance Criteria**:

1. WHEN o funcionário cadastra um item com SKU novo THEN o sistema SHALL persistir com situação ATIVO, saldo físico igual ao saldo inicial, saldo reservado zero e responder 201.
2. IF o SKU já existe THEN o sistema SHALL responder 409 sem persistir.
3. IF o preço de venda é menor ou igual a zero ou o estoque mínimo é negativo THEN o sistema SHALL responder 422.
4. WHEN o funcionário atualiza nome, preço de venda ou estoque mínimo THEN o sistema SHALL persistir sem alterar os saldos.
5. WHEN o funcionário consulta um item THEN o sistema SHALL retornar saldo físico, saldo reservado e saldo disponível.
6. WHEN o funcionário inativa um item THEN o sistema SHALL mudar a situação para INATIVO e manter o histórico.

**Independent Test**: Cadastrar SKU novo (201) e repetido (409).

---

### P1: Entrada de estoque ⭐ MVP

**User Story**: Como funcionário, quero registrar entradas manuais para repor o saldo.

**Why P1**: Única forma de reposição no MVP.

**Acceptance Criteria**:

1. WHEN o funcionário registra entrada de quantidade positiva THEN o sistema SHALL somar a quantidade ao saldo físico.
2. IF a quantidade é menor ou igual a zero THEN o sistema SHALL responder 422.

**Independent Test**: Saldo físico 10, entrada 5, saldo físico 15.

---

### P1: Disponibilidade e preço ⭐ MVP

**User Story**: Como OS, quero saber preço de venda e disponibilidade dos itens pedidos para montar o orçamento.

**Why P1**: Fase 3 do fluxo de estoque; porta `EstoqueDeItens`.

**Acceptance Criteria**:

1. The sistema SHALL calcular saldo disponível como saldo físico − saldo reservado, nunca menor que zero.
2. WHEN a disponibilidade de um item é consultada com uma quantidade THEN o sistema SHALL retornar o preço de venda atual e "disponível" se saldo disponível ≥ quantidade, ou "indisponível" caso contrário.
3. IF o item consultado está INATIVO ou não existe THEN o sistema SHALL lançar `DomainException` informando o SKU.

**Independent Test**: Físico 10, reservado 2, consulta 8 disponível e consulta 9 indisponível.

---

### P1: Reserva por OS ⭐ MVP

**User Story**: Como OS, quero reservar os itens do orçamento aprovado para que não sejam usados por outra OS.

**Why P1**: Decisões do MVP: reserva na aprovação.

**Acceptance Criteria**:

1. WHEN a reserva de itens de uma OS é pedida e todos têm saldo disponível suficiente THEN o sistema SHALL somar cada quantidade ao saldo reservado do item e registrar os itens na Reserva da OS.
2. IF algum item do pedido não tem saldo disponível suficiente THEN o sistema SHALL não reservar nenhum item e retornar a lista de SKUs com quantidade pedida e saldo disponível.
3. IF algum item do pedido está INATIVO THEN o sistema SHALL não reservar nenhum item e lançar `DomainException`.
4. WHEN uma nova reserva é pedida para OS que já possui Reserva THEN o sistema SHALL acumular os novos itens na mesma Reserva.

**Independent Test**: Duas OS disputando o último item: a segunda recebe a falta e nada é reservado.

---

### P1: Consumo na finalização ⭐ MVP

**User Story**: Como OS finalizada, quero baixar o estoque dos itens reservados.

**Why P1**: Glossário "Consumo"; estoque 7.1.

**Acceptance Criteria**:

1. WHEN o consumo da Reserva de uma OS é pedido THEN o sistema SHALL subtrair cada quantidade reservada do saldo físico e do saldo reservado do item e marcar a Reserva como consumida.
2. IF a Reserva da OS já foi consumida THEN o sistema SHALL lançar `DomainException` sem alterar saldos.
3. WHEN o saldo físico de um item fica abaixo do estoque mínimo após o consumo THEN o sistema SHALL notificar o funcionário com SKU, saldo físico e estoque mínimo.

**Independent Test**: Físico 10, reservado 3, mínimo 8: após consumo físico 7, reservado 0, funcionário notificado.

---

### P2: Descarte de insumo comprometido

**User Story**: Como funcionário, quero descartar insumos vencidos ou danificados.

**Why P2**: Glossário "Descarte"; não bloqueia o fluxo da OS.

**Acceptance Criteria**:

1. WHEN o funcionário descarta quantidade de um INSUMO com motivo THEN o sistema SHALL subtrair a quantidade do saldo físico e registrar o descarte com data e motivo.
2. IF o item é do tipo PECA THEN o sistema SHALL responder 422.
3. IF a quantidade é maior que o saldo disponível THEN o sistema SHALL responder 422 sem alterar o saldo.
4. WHEN o saldo físico fica abaixo do estoque mínimo após o descarte THEN o sistema SHALL notificar o funcionário.

**Independent Test**: Físico 5, reservado 3: descartar 3 é rejeitado; descartar 2 deixa físico 3.

---

### P2: Devolução ao estoque

**User Story**: Como OS cancelada por inadimplência, quero devolver as peças recuperadas ao estoque.

**Why P2**: Usado só no escopo `inadimplencia` (AD-007).

**Acceptance Criteria**:

1. WHEN a devolução de peças de uma OS é pedida THEN o sistema SHALL somar cada quantidade ao saldo físico.
2. IF a quantidade devolvida de um item é maior que a quantidade consumida por aquela OS, ou o item é INSUMO THEN o sistema SHALL lançar `DomainException` sem alterar saldos.

**Independent Test**: OS consumiu 2 pastilhas; devolver 3 é rejeitado; devolver 2 soma 2 ao físico.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| EST-01 | P1: Cadastro de itens | - | Pending |
| EST-02 | P1: Entrada de estoque | - | Pending |
| EST-03 | P1: Disponibilidade e preço | - | Pending |
| EST-04 | P1: Reserva por OS | - | Pending |
| EST-05 | P1: Consumo na finalização | - | Pending |
| EST-06 | P2: Descarte de insumo | - | Pending |
| EST-07 | P2: Devolução ao estoque | - | Pending |

**Coverage:** 7 total, 0 mapped tasks, 7 unmapped ⚠️

---

## Success Criteria

- [ ] Nenhuma sequência de operações deixa saldo disponível negativo (teste de domínio).
