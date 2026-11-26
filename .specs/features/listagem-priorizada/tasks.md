# Listagem Priorizada de OS Tasks

## Execution Protocol (MANDATORY -- do not skip)

Implement these tasks with the `tlc-spec-driven` skill: **activate it by name and follow its Execute flow and Critical Rules.** Do not search for skill files by filesystem path. The skill is the source of truth for the full flow (per-task cycle, sub-agent delegation, adequacy review, Verifier, discrimination sensor).

**If the skill cannot be activated, STOP and tell the user - do not proceed without it.**

---

**Design**: inline (sem `design.md`: uma query nova no repositório, AD-009)
**Status**: Draft

---

## Test Coverage Matrix

Guidelines found: `AGENTS.md` (TDD, F.I.R.S.T., `*Test`/`*IT`, JaCoCo 80% em `domain` e `application`), `pom.xml` (failsafe, jacoco).

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Application | unit | Delegação ao repositório e conversão para DTO | `src/test/java/br/com/fiap/oficina/application/ordemservico/*Test.java` | `./mvnw test` |
| Repository | integration | Ordem por status, por `abertaEm` e `id`, exclusão de `FINALIZADA`/`ENTREGUE`, paginação e `totalElementos` | `src/test/java/br/com/fiap/oficina/infrastructure/db/**/*IT.java` | `./mvnw verify` |
| Controller | integration | Rota com a ordem nova e detalhe de OS oculta ainda acessível | `src/test/java/br/com/fiap/oficina/infrastructure/http/**/*IT.java` | `./mvnw verify` |

## Gate Check Commands

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | Tasks de domain/application | `./mvnw test` |
| Full | Tasks com `*IT` | `./mvnw verify` |
| Build | Fim de fase | `./mvnw verify` |

---

## Execution Plan

### Phase 1: Consulta e rota

```
T1 → T2
```

---

## Task Breakdown

### T1: Listar OS por prioridade no repositório

**What**: Trocar `OrdemServicoRepository.listar` por `listarPorPrioridade(pagina, tamanho)`. A implementação JPA usa `@Query` com `where status not in (FINALIZADA, ENTREGUE)` e `order by case status (EM_EXECUCAO 0, AGUARDANDO_APROVACAO 1, EM_DIAGNOSTICO 2, RECEBIDA 3, CANCELADA 4), abertaEm, id`, mais `countQuery` com o mesmo filtro. `ListarOrdensServico` e `OrdemServicoRepositoryEmMemoria` passam a usar o nome novo; o repositório em memória aplica a mesma ordem com um `Comparator`.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/db/ordemservico/OrdemServicoRepositoryImpl.java` (modify)
**Depends on**: None
**Reuses**: `paraPagina`, `OrdemServicoJpaRepository`
**Requirement**: LST-01, LST-02, LST-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `OrdemServicoRepositoryImplIT`: OS nos 7 status, com datas variadas, voltam na ordem de LST-01.1; mesmo status sai por `abertaEm` crescente e, com data igual, por `id` (LST-01.2); `FINALIZADA` e `ENTREGUE` ausentes (LST-01.3); `totalElementos` conta só as listadas, e a segunda página continua a ordem (LST-01.5)
- [ ] `ListarOrdensServicoTest` atualizado para `listarPorPrioridade`
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

**Commit**: `feat(ordemservico): lista OS por prioridade de status sem finalizadas e entregues`

---

### T2: Documentar a listagem e cobrir a rota

**What**: Atualizar o `@Operation` de `GET /ordens-servico` com a ordem e a exclusão. Ajustar `AcompanhamentoAdminIT` à ordem nova e verificar que uma OS `ENTREGUE` some da listagem e continua em `GET /ordens-servico/{id}` (LST-01.4).
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/http/ordemservico/OrdemServicoController.java` (modify)
**Depends on**: T1
**Reuses**: `AcompanhamentoAdminIT`
**Requirement**: LST-01, LST-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `AcompanhamentoAdminIT` cobre a ordem da rota, a ausência de `ENTREGUE` na listagem e o 200 do detalhe dela
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

**Commit**: `docs(ordemservico): descreve a ordem da listagem de OS no swagger`
