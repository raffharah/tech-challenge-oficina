# Execução e Entrega Tasks

## Execution Protocol (MANDATORY -- do not skip)

Implement these tasks with the `tlc-spec-driven` skill: **activate it by name and follow its Execute flow and Critical Rules.** Do not search for skill files by filesystem path. The skill is the source of truth for the full flow (per-task cycle, sub-agent delegation, adequacy review, Verifier, discrimination sensor).

**If the skill cannot be activated, STOP and tell the user - do not proceed without it.**

---

**Design**: `.specs/features/abertura-os-orcamento/design.md` (agregado OS)
**Status**: Reviewed

---

## Test Coverage Matrix

> Same matrix as `fundacao-plataforma/tasks.md` - confirm before Execute.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Domain | unit | Todos os ramos; 1:1 com ACs | `src/test/java/br/com/fiap/oficina/domain/ordemservico/*Test.java` | `./mvnw test` |
| Application | unit | 1:1 com ACs e erros | `src/test/java/br/com/fiap/oficina/application/ordemservico/*Test.java` | `./mvnw test` |
| Controller | integration | Cada rota: sucesso + erros | `src/test/java/br/com/fiap/oficina/infrastructure/**/*IT.java` | `./mvnw verify` |

## Gate Check Commands

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | Tasks de domain/application | `./mvnw test` |
| Full | Tasks com `*IT` | `./mvnw verify` |
| Build | Fim de fase | `./mvnw verify` |

---

## Execution Plan

### Phase 1: Execução e entrega

```
T1 → T2 → T3 → T4
```

---

## Task Breakdown

### T1: Adicionar finalização e entrega à OrdemServico

**What**: `finalizar(Instant)` (EM_EXECUCAO → FINALIZADA) e `entregar(Instant)` (FINALIZADA → ENTREGUE).
**Where**: `src/main/java/br/com/fiap/oficina/domain/ordemservico/OrdemServico.java` (modify)
**Depends on**: None
**Reuses**: `mudarPara` (abertura T3)
**Requirement**: EXE-01, EXE-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `OrdemServicoTest`: finalização e entrega registram data/hora no histórico (EXE-01.1, EXE-02.1); fora do status exigido → `DomainException` (EXE-01.2, EXE-02.2)
- [x] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T2: Criar caso de uso FinalizarOrdemServico

**What**: Finaliza a OS com `Clock`.
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/FinalizarOrdemServico.java`
**Depends on**: T1
**Reuses**: `OrdemServicoRepositoryEmMemoria`
**Requirement**: EXE-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `FinalizarOrdemServicoTest`: FINALIZADA com data do `Clock` fixo (EXE-01.1); EM_DIAGNOSTICO → 422 (EXE-01.2); inexistente → 404
- [x] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T3: Criar caso de uso EntregarVeiculo

**What**: Registra a entrega da OS com `Clock`.
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/EntregarVeiculo.java`
**Depends on**: T2
**Reuses**: `OrdemServicoRepositoryEmMemoria`
**Requirement**: EXE-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `EntregarVeiculoTest`: ENTREGUE (EXE-02.1); EM_EXECUCAO → 422 (EXE-02.2); inexistente → 404
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T4: Criar endpoints de finalização e entrega

**What**: `POST /ordens-servico/{id}/finalizacao` e `POST /ordens-servico/{id}/entrega`; registra os casos de uso em `BeanConfig`.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/http/ordemservico/OrdemServicoController.java` (modify)
**Depends on**: T3
**Reuses**: padrão do `OrdemServicoController`
**Requirement**: EXE-01, EXE-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `ExecucaoEntregaIT` (com JWT): fluxo abertura → orçamento → aprovação → finalização → entrega termina ENTREGUE; finalizar OS em diagnóstico 422; entregar OS em execução 422; sem token 401
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full
