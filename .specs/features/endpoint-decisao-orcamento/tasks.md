# Endpoint Único de Decisão do Orçamento Tasks

## Execution Protocol (MANDATORY -- do not skip)

Implement these tasks with the `tlc-spec-driven` skill: **activate it by name and follow its Execute flow and Critical Rules.** Do not search for skill files by filesystem path. The skill is the source of truth for the full flow (per-task cycle, sub-agent delegation, adequacy review, Verifier, discrimination sensor).

**If the skill cannot be activated, STOP and tell the user - do not proceed without it.**

---

**Design**: inline (sem `design.md`). A rota nova escolhe entre `AprovarOrcamento` e `RejeitarOrcamento`, que já existem, pelo campo `decisao`. Domínio e application não mudam (AD-010).
**Status**: Draft

---

## Test Coverage Matrix

Guidelines found: `AGENTS.md`, `pom.xml`.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Request | none | Validação coberta pela IT da rota | - | build gate only |
| Controller | integration | Aprovação, recusa, falta de saldo, transição inválida, outro titular, corpo inválido, rotas antigas | `src/test/java/br/com/fiap/oficina/infrastructure/http/ordemservico/*IT.java` | `./mvnw verify` |

## Gate Check Commands

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | Tasks de domain/application | `./mvnw test` |
| Full | Tasks com `*IT` | `./mvnw verify` |
| Build | Fim de fase ou task sem teste | `./mvnw verify` |

---

## Execution Plan

### Phase 1: Rota única

```
T1 → T2
```

---

## Task Breakdown

### T1: Incluir a decisão no corpo da requisição

**What**: Adicionar a `DecisaoOrcamentoRequest` o campo `@NotNull Decisao decisao`, com o enum aninhado `Decisao { APROVADO, RECUSADO }` e `@Schema` de exemplo.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/http/ordemservico/DecisaoOrcamentoRequest.java` (modify)
**Depends on**: None
**Reuses**: `ErroHandler`, que já converte `HttpMessageNotReadableException` e `MethodArgumentNotValidException` em 400
**Requirement**: DEC-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] Compila; validação coberta em T2
- [ ] Gate check passes: `./mvnw verify`

**Tests**: none
**Gate**: build

**Commit**: `feat(ordemservico): inclui a decisao no corpo da requisicao do orcamento`

---

### T2: Substituir aprovação e rejeição por `POST /{id}/decisao`

**What**: Em `ClienteOrdemServicoController`, trocar `aprovar` e `rejeitar` por `decidir`: `APROVADO` chama `AprovarOrcamento` e `RECUSADO` chama `RejeitarOrcamento`, com `@Operation` e `@ApiResponse` (200, 400, 404, 422). Migrar as chamadas a `/aprovacao` e `/rejeicao` em `DecisaoOrcamentoIT`, `ConsultaClienteIT`, `ExecucaoEntregaIT` e `AcompanhamentoAdminIT`.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/http/ordemservico/ClienteOrdemServicoController.java` (modify)
**Depends on**: T1
**Reuses**: `AprovarOrcamento`, `RejeitarOrcamento`
**Requirement**: DEC-01, DEC-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `DecisaoOrcamentoIT`:
  - `APROVADO` com saldo leva a OS a `EM_EXECUCAO` com saldo baixado (DEC-01.1);
  - `RECUSADO` leva a `CANCELADA` com saldo intacto (DEC-01.2);
  - falta de saldo responde 422 `SALDO_INSUFICIENTE` e nada muda (DEC-01.3);
  - OS fora de `AGUARDANDO_APROVACAO` responde 422 `TRANSICAO_INVALIDA` (DEC-01.4);
  - outro titular responde 404 (DEC-01.5);
  - `decisao` ausente ou `"TALVEZ"` responde 400 (DEC-01.6);
  - CPF/CNPJ inválido responde 422 (DEC-01.7);
  - `POST /aprovacao` e `/rejeicao` respondem 404 com a OS intacta (DEC-02.1).
- [ ] As demais ITs passam a usar `/decisao`
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

**Commit**: `feat(ordemservico): recebe aprovacao e recusa do orcamento por endpoint unico`
