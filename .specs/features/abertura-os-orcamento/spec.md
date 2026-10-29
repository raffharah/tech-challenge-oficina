# Abertura da OS e Orçamento Specification

## Problem Statement

Atendimento e orçamento são feitos em anotações. O MVP exige criar a OS a partir do cliente identificado por CPF/CNPJ e do veículo, incluir serviços, peças e insumos, gerar o orçamento automaticamente e disponibilizá-lo para aprovação, com status controlados por regras de transição.

## Goals

- [ ] OS vai de Recebida a Aguardando aprovação com orçamento calculado.
- [ ] Ciclo de vida da OS com transições validadas no domínio.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Envio do orçamento por e-mail | Requisito aceita "disponibilização"; feita pela consulta do cliente (ACO-01) |
| Cadastro de cliente/veículo dentro da abertura | Feito antes pelo CRUD |
| Prazo, histórico de orçamentos, renegociação | AD-003 |
| Verificação de saldo na inclusão | Saldo é verificado na aprovação (AD-004) |
| Desconto e impostos | Orçamento é somatório simples |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Dados da abertura | CPF/CNPJ e placa | Mínimo para identificar cliente e veículo | y |
| Recebida → Em diagnóstico | Automática na abertura; histórico registra as duas transições | Política do Event Storming; "alteração automática dos status" | y |
| Preço do componente | Copiado na inclusão (valor padrão do serviço, preço de venda do item) | Mudança de catálogo não altera OS em andamento | y |
| Mesmo item incluído duas vezes | Quantidades somadas | Evita linhas duplicadas | y |
| Status CANCELADA | Existe só para orçamento rejeitado | Rejeição precisa de estado final (ORC-02) | y |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Abrir OS ⭐ MVP

**User Story**: Como funcionário, quero abrir uma OS para o cliente e o veículo identificados.

**Why P1**: Início da criação da OS.

**Acceptance Criteria**:

1. WHEN o funcionário abre OS com CPF/CNPJ de cliente cadastrado e placa de veículo desse cliente THEN o sistema SHALL criar a OS RECEBIDA, mudá-la para EM_DIAGNOSTICO e responder 201 com o número da OS.
2. IF o CPF/CNPJ ou a placa é inválido THEN o sistema SHALL responder 422 sem criar a OS.
3. IF o cliente ou o veículo não está cadastrado THEN o sistema SHALL responder 404.
4. IF o veículo pertence a outro cliente THEN o sistema SHALL responder 422.

**Independent Test**: Abrir OS e conferir status EM_DIAGNOSTICO.

---

### P1: Incluir serviços, peças e insumos ⭐ MVP

**User Story**: Como mecânico, quero incluir e remover serviços, peças e insumos durante o diagnóstico.

**Why P1**: Itens obrigatórios da criação da OS.

**Acceptance Criteria**:

1. WHILE a OS está EM_DIAGNOSTICO, WHEN o mecânico inclui serviço do catálogo THEN o sistema SHALL adicioná-lo com o valor padrão vigente.
2. WHILE a OS está EM_DIAGNOSTICO, WHEN o mecânico inclui peça ou insumo com quantidade positiva THEN o sistema SHALL adicioná-lo com o preço de venda obtido pela porta `EstoqueDeItens`.
3. WHILE a OS está EM_DIAGNOSTICO, WHEN o mecânico remove componente THEN o sistema SHALL retirá-lo da OS.
4. IF o serviço ou item não existe THEN o sistema SHALL responder 422.
5. IF a OS não está EM_DIAGNOSTICO THEN o sistema SHALL rejeitar inclusão e remoção com 422.
6. IF o serviço já está na OS THEN o sistema SHALL responder 422 sem duplicá-lo.

**Independent Test**: Incluir troca de óleo + 4 litros de óleo; incluir em OS fora de diagnóstico (422).

---

### P1: Gerar orçamento ⭐ MVP

**User Story**: Como mecânico, quero gerar o orçamento automaticamente e deixá-lo disponível para aprovação.

**Why P1**: Geração automática e disponibilização são obrigatórias.

**Acceptance Criteria**:

1. WHILE a OS está EM_DIAGNOSTICO, WHEN o mecânico conclui o diagnóstico THEN o sistema SHALL calcular o total como Σ valor dos serviços + Σ (quantidade × preço) dos itens e mudar a OS para AGUARDANDO_APROVACAO.
2. IF a OS não tem nenhum serviço THEN o sistema SHALL responder 422 sem calcular.
3. The sistema SHALL calcular valores em `BigDecimal` com duas casas e arredondamento HALF_EVEN.

**Independent Test**: Serviço R$ 100,00 + 2 itens de R$ 35,50: total R$ 171,00 e OS AGUARDANDO_APROVACAO.

---

### P1: Regras de transição ⭐ MVP

**User Story**: Como sistema, quero aceitar só as transições do ciclo de vida da OS.

**Why P1**: "Controlar alterações de status" e "aplicar regras de transição" são obrigatórios.

**Acceptance Criteria**:

1. The sistema SHALL permitir somente RECEBIDA→EM_DIAGNOSTICO, EM_DIAGNOSTICO→AGUARDANDO_APROVACAO, AGUARDANDO_APROVACAO→EM_EXECUCAO, AGUARDANDO_APROVACAO→CANCELADA, EM_EXECUCAO→FINALIZADA e FINALIZADA→ENTREGUE.
2. IF transição fora dessa lista é pedida THEN o sistema SHALL lançar `DomainException` sem alterar o status.
3. WHEN uma transição acontece THEN o sistema SHALL registrar novo status e data/hora no histórico da OS.

**Independent Test**: Teste de domínio parametrizado com todas as combinações de status.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| OS-01 | P1: Abrir OS | Execute | Verified |
| OS-02 | P1: Incluir serviços, peças e insumos | Execute | Verified |
| OS-03 | P1: Gerar orçamento | Execute | Verified |
| OS-04 | P1: Regras de transição | Execute | Verified |

**Coverage:** 4 total, 4 mapped tasks, 0 unmapped

---

## Success Criteria

- [ ] Abertura → diagnóstico → orçamento demonstrável pelo Swagger.
