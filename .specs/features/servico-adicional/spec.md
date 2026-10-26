# Serviço Adicional Specification

## Problem Statement

Durante a execução o mecânico pode encontrar outro problema no veículo. A OS volta ao diagnóstico, um novo orçamento é enviado e o cliente aceita (o serviço retoma com o acréscimo) ou rejeita (retoma o orçamento anterior). Requisito do desafio: "autorizar reparos adicionais" (fluxo-os 4.2).

## Goals

- [ ] Novo orçamento versionado; aceite reserva só o acréscimo; rejeição retoma a versão anterior sem cancelar a OS (AD-006).

## Out of Scope

| Feature | Reason |
| --- | --- |
| Recusa parcial do novo orçamento | Fluxo 4.2 só tem aceitar ou rejeitar |
| Remoção de componentes já aprovados | Serviço adicional só acrescenta |
| Peça defeituosa e garantia | Fora do MVP (estoque 7.3) |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Registro do problema | Um comando do mecânico com descrição do problema, serviços/itens adicionais e prazo opcional; leva a OS de EM_EXECUCAO para EM_DIAGNOSTICO e, com o novo orçamento enviado, para AGUARDANDO_APROVACAO | Políticas automáticas do fluxo 4.2 (AD-002) | n |
| Serviços já iniciados ou concluídos | Mantêm datas e situação durante o adicional | O fluxo retoma o serviço, não o reinicia | n |
| Novo orçamento | Versão nova = componentes aprovados + adicionais; total recalculado; prazo novo ou o anterior | Política "recalcula o orçamento com as novas condições" | n |
| Falta de estoque no aceite | Mesmo comportamento da aprovação: nada reservado, OS continua AGUARDANDO_APROVACAO, funcionário notificado, 422 | Coerência com `decisao-orcamento` | n |
| Canal da resposta | Endpoints `/cliente` sem JWT | AD-001 | y |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Registrar serviço adicional ⭐ MVP

**User Story**: Como mecânico, quero registrar um problema encontrado e propor serviços adicionais.

**Why P1**: Início do desvio 4.2.

**Acceptance Criteria**:

1. WHILE a OS está EM_EXECUCAO, WHEN o mecânico registra um problema com ao menos um serviço adicional THEN o sistema SHALL mudar a OS para EM_DIAGNOSTICO, registrar os componentes adicionais e o problema.
2. WHEN os adicionais são registrados THEN o sistema SHALL calcular o novo orçamento como nova versão com situação ENVIADO, enviá-lo ao cliente por e-mail e mudar a OS para AGUARDANDO_APROVACAO.
3. IF a OS não está EM_EXECUCAO THEN o sistema SHALL responder 422.
4. IF nenhum serviço adicional é informado THEN o sistema SHALL responder 422.

**Independent Test**: OS EM_EXECUCAO com um serviço; registrar adicional; OS AGUARDANDO_APROVACAO com orçamento versão n+1.

---

### P1: Aceitar novo orçamento ⭐ MVP

**User Story**: Como cliente, quero aceitar o novo orçamento para incluir o reparo adicional.

**Why P1**: Ramo "aceita" do fluxo 4.2.

**Acceptance Criteria**:

1. WHILE a OS está AGUARDANDO_APROVACAO por serviço adicional, WHEN o cliente aceita THEN o sistema SHALL reservar só os itens adicionais e mudar o novo orçamento para APROVADO.
2. WHEN a reserva é concluída THEN o sistema SHALL notificar o funcionário e mudar a OS para EM_EXECUCAO.
3. IF falta saldo para algum item adicional THEN o sistema SHALL manter a OS AGUARDANDO_APROVACAO, notificar o funcionário e responder 422 com os itens faltantes.

**Independent Test**: Aceitar e conferir saldo reservado acrescido só do item novo.

---

### P1: Rejeitar novo orçamento ⭐ MVP

**User Story**: Como cliente, quero rejeitar o adicional e manter o serviço combinado antes.

**Why P1**: Ramo "rejeita" do fluxo 4.2.

**Acceptance Criteria**:

1. WHILE a OS está AGUARDANDO_APROVACAO por serviço adicional, WHEN o cliente rejeita THEN o sistema SHALL mudar o novo orçamento para REJEITADO, remover os componentes adicionais e tornar vigente o orçamento anterior com seu prazo.
2. WHEN o orçamento anterior é retomado THEN o sistema SHALL notificar o funcionário e mudar a OS para EM_EXECUCAO sem reservar itens.
3. IF o cliente pede recusa parcial do novo orçamento THEN o sistema SHALL responder 422.
4. The sistema SHALL nunca cancelar a OS pela rejeição de um orçamento de serviço adicional.

**Independent Test**: Rejeitar e conferir orçamento vigente = versão anterior, OS EM_EXECUCAO, saldo reservado inalterado.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| ADC-01 | P1: Registrar serviço adicional | - | Pending |
| ADC-02 | P1: Aceitar novo orçamento | - | Pending |
| ADC-03 | P1: Rejeitar novo orçamento | - | Pending |

**Coverage:** 3 total, 0 mapped tasks, 3 unmapped ⚠️

---

## Success Criteria

- [ ] Histórico de orçamentos da OS mostra todas as versões com situação e total.
