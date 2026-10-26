# Decisão sobre o Orçamento Tasks

## Execution Protocol (MANDATORY -- do not skip)

Implement these tasks with the `tlc-spec-driven` skill: **activate it by name and follow its Execute flow and Critical Rules.** Do not search for skill files by filesystem path. The skill is the source of truth for the full flow (per-task cycle, sub-agent delegation, adequacy review, Verifier, discrimination sensor).

**If the skill cannot be activated, STOP and tell the user - do not proceed without it.**

---

**Design**: `.specs/features/abertura-os-orcamento/design.md` (agregado OS, porta `EstoqueDeItens`, endpoints `/cliente`)
**Status**: Draft

---

## Test Coverage Matrix

> Same matrix as `fundacao-plataforma/tasks.md` - confirm before Execute.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Domain | unit | Todos os ramos; 1:1 com ACs | `src/test/java/br/com/fiap/oficina/domain/ordemservico/*Test.java` | `./mvnw test` |
| Application | unit | 1:1 com ACs e erros; portas em memória | `src/test/java/br/com/fiap/oficina/application/ordemservico/*Test.java` | `./mvnw test` |
| Controller / Gateway | integration | Cada rota: sucesso + bordas + erros; rollback da transação | `src/test/java/br/com/fiap/oficina/infrastructure/**/*IT.java` | `./mvnw verify` |

## Gate Check Commands

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | Tasks de domain/application | `./mvnw test` |
| Full | Tasks com `*IT` | `./mvnw verify` |
| Build | Fim de fase | `./mvnw verify` |

---

## Execution Plan

### Phase 1: Domínio e casos de uso

```
T1 → T2 → T3
```

### Phase 2: Infraestrutura

```
T4 → T5
```

---

## Task Breakdown

### T1: Adicionar aprovação e rejeição à OrdemServico

**What**: `aprovar(Instant)` (AGUARDANDO_APROVACAO → EM_EXECUCAO) e `rejeitar(Instant)` (→ CANCELADA).
**Where**: `src/main/java/br/com/fiap/oficina/domain/ordemservico/OrdemServico.java` (modify)
**Depends on**: None
**Reuses**: `mudarPara` (abertura T3)
**Requirement**: ORC-01, ORC-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `OrdemServicoTest`: aprovação e rejeição registram histórico; ambas fora de AGUARDANDO_APROVACAO → `DomainException` (ORC-01.3); OS CANCELADA rejeita inclusão, conclusão e aprovação (ORC-02.2)
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T2: Criar caso de uso AprovarOrcamento

**What**: Chama `EstoqueDeItens.baixar(itens da OS)` e só depois `aprovar`; acrescenta `baixar` à porta.
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/AprovarOrcamento.java`
**Depends on**: T1
**Reuses**: `EstoqueDeItensEmMemoria` (abertura T9), `OrdemServicoRepositoryEmMemoria`
**Requirement**: ORC-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `AprovarOrcamentoTest`: com saldo → baixa chamada e EM_EXECUCAO (ORC-01.1); porta lança falta → status AGUARDANDO_APROVACAO e OS não salva (ORC-01.2); OS em outro status → 422 sem chamar a porta (ORC-01.3); inexistente → 404
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T3: Criar caso de uso RejeitarOrcamento

**What**: Rejeita o orçamento e cancela a OS.
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/RejeitarOrcamento.java`
**Depends on**: T2
**Reuses**: `OrdemServicoRepositoryEmMemoria`
**Requirement**: ORC-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `RejeitarOrcamentoTest`: CANCELADA (ORC-02.1); fora de AGUARDANDO_APROVACAO → 422; inexistente → 404
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T4: Implementar EstoqueDeItens.baixar

**What**: `EstoqueDeItensImpl.baixar` converte `ItemDaOrdem` em pedido de `BaixarItens`; a `DomainException` de falta (com `detalhes`) é repassada.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/gateway/estoque/EstoqueDeItensImpl.java` (modify)
**Depends on**: None
**Reuses**: `BaixarItens` (estoque T7)
**Requirement**: ORC-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `EstoqueDeItensImplIT`: baixa reduz saldo físico; falta lança `DomainException` com `detalhes` e saldo inalterado
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

---

### T5: Criar endpoints de decisão do cliente

**What**: `ClienteOrdemServicoController` com `POST /cliente/ordens-servico/{id}/aprovacao` e `.../rejeicao` (sem JWT, `@Transactional`); registra os casos de uso em `BeanConfig`.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/http/ordemservico/ClienteOrdemServicoController.java`
**Depends on**: T4
**Reuses**: `ErroHandler`
**Requirement**: ORC-01, ORC-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `DecisaoOrcamentoIT` (sem token): aprovação com saldo → EM_EXECUCAO e saldo baixado; sem saldo → 422 com itens em `detalhes`, OS AGUARDANDO_APROVACAO e nenhum saldo alterado; rejeição → CANCELADA e inclusão posterior 422; status inválido 422; inexistente 404
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full
