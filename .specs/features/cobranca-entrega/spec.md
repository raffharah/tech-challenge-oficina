# Cobrança, Nota Fiscal e Entrega Specification

## Problem Statement

Depois da finalização a oficina precisa cobrar, registrar o pagamento, emitir a nota fiscal e entregar o veículo (fluxo-os 5.1). Pagamento e nota fiscal são sistemas externos; no MVP são portas com implementação simulada que expõem payload e resposta (Decisões do MVP).

## Goals

- [ ] Cobrança gerada na finalização, pagamento registrado, NF emitida e veículo entregue com OS ENTREGUE.
- [ ] Payload e resposta simulada de pagamento e NF consultáveis pela API.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Pagamento parcelado ou parcial | Valor único igual ao orçamento vigente |
| Integração real com gateway e SEFAZ | Portas simuladas (Decisões do MVP) |
| Atraso, vencimento e cancelamento por inadimplência | Escopo `inadimplencia` |
| Estorno e cancelamento de NF | Não aparecem nos fluxos |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Agregados | Pagamento (cobrança + registro) e Nota Fiscal no contexto `cobranca`; a OS só registra que foi paga | Decisões do MVP: agregados | y |
| Gatilho da cobrança | Na finalização da OS, após notificar o cliente, na mesma transação | Política "Quando o cliente for notificado da finalização, enviar a cobrança" (AD-002) | n |
| Valor da cobrança | Total do orçamento vigente (último APROVADO) | Glossário "Orçamento" em Cobrança | n |
| Efetuar pagamento | Endpoint `/cliente` sem JWT simula o pagamento do cliente; o sistema chama a porta `SistemaDePagamentos` e registra a resposta | AD-001 | y |
| Estados do Pagamento | A_VENCER, EM_ATRASO, VENCIDO, PAGO | Glossário Cobrança | y |
| Entrega sem pagamento | Rejeitada com 422 | Glossário: entrega ocorre após o pagamento | n |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Enviar cobrança ⭐ MVP

**User Story**: Como sistema, quero cobrar o cliente quando a OS é finalizada.

**Why P1**: Início da fase 3 do fluxo da OS.

**Acceptance Criteria**:

1. WHEN a OS é finalizada THEN o sistema SHALL criar um Pagamento A_VENCER com valor igual ao total do orçamento vigente e data de referência igual à data da finalização.
2. WHEN o Pagamento é criado THEN o sistema SHALL registrar a cobrança na porta `SistemaDePagamentos` e enviar ao cliente por e-mail o valor e as instruções de pagamento retornadas.
3. The sistema SHALL ter no máximo um Pagamento por OS.

**Independent Test**: Finalizar OS e consultar o Pagamento A_VENCER e o e-mail de cobrança.

---

### P1: Registrar pagamento e emitir NF ⭐ MVP

**User Story**: Como cliente, quero pagar e receber a nota fiscal.

**Why P1**: Caminho principal 5.1, passos 3 a 7.

**Acceptance Criteria**:

1. WHILE o Pagamento está A_VENCER ou EM_ATRASO, WHEN o cliente efetua o pagamento THEN o sistema SHALL confirmar pela porta `SistemaDePagamentos`, mudar o Pagamento para PAGO com data/hora e registrar o pagamento na OS sem alterar o status da OS.
2. WHEN o pagamento é registrado THEN o sistema SHALL emitir a Nota Fiscal pela porta de nota fiscal com CPF/CNPJ do cliente, componentes e total, e persistir payload e resposta simulada.
3. WHEN a Nota Fiscal é emitida THEN o sistema SHALL enviar a NF ao cliente por e-mail e, em seguida, a notificação de retirada do veículo.
4. IF o Pagamento está PAGO ou VENCIDO THEN o sistema SHALL responder 422 sem chamar a porta.
5. WHEN o funcionário consulta pagamento ou NF de uma OS THEN o sistema SHALL retornar o payload enviado e a resposta simulada.

**Independent Test**: Pagar: Pagamento PAGO, NF emitida, e-mails de NF e retirada registrados; pagar de novo (422).

---

### P1: Entregar veículo ⭐ MVP

**User Story**: Como funcionário, quero registrar a entrega do veículo ao cliente.

**Why P1**: Último status obrigatório (Entregue).

**Acceptance Criteria**:

1. WHILE a OS está FINALIZADA e o Pagamento está PAGO, WHEN o funcionário registra a entrega THEN o sistema SHALL mudar a OS para ENTREGUE com data/hora.
2. IF o Pagamento da OS não está PAGO THEN o sistema SHALL responder 422 sem alterar a OS.

**Independent Test**: Tentar entregar antes de pagar (422) e depois (ENTREGUE).

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| COB-01 | P1: Enviar cobrança | - | Pending |
| COB-02 | P1: Registrar pagamento e emitir NF | - | Pending |
| COB-03 | P1: Entregar veículo | - | Pending |

**Coverage:** 3 total, 0 mapped tasks, 3 unmapped ⚠️

---

## Success Criteria

- [ ] Fluxo completo abertura → entrega demonstrável pelo Swagger.
