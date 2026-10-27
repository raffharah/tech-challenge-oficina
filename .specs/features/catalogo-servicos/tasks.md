# Catálogo de Serviços Tasks

## Execution Protocol (MANDATORY -- do not skip)

Implement these tasks with the `tlc-spec-driven` skill: **activate it by name and follow its Execute flow and Critical Rules.** Do not search for skill files by filesystem path. The skill is the source of truth for the full flow (per-task cycle, sub-agent delegation, adequacy review, Verifier, discrimination sensor).

**If the skill cannot be activated, STOP and tell the user - do not proceed without it.**

---

**Design**: `.specs/features/fundacao-plataforma/design.md` (padrões; escopo CRUD sem design próprio)
**Status**: Reviewed

---

## Test Coverage Matrix

> Same matrix as `fundacao-plataforma/tasks.md` - confirm before Execute.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Domain | unit | Todos os ramos; 1:1 com ACs | `src/test/java/br/com/fiap/oficina/domain/ordemservico/*Test.java` | `./mvnw test` |
| Application | unit | 1:1 com ACs e erros; repositório em memória | `src/test/java/br/com/fiap/oficina/application/ordemservico/*Test.java` | `./mvnw test` |
| Controller / RepositoryImpl | integration | Cada rota: sucesso + bordas + erros | `src/test/java/br/com/fiap/oficina/infrastructure/**/*IT.java` | `./mvnw verify` |
| Migration | none | build gate | - | `./mvnw verify` |

## Gate Check Commands

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | Tasks de domain/application | `./mvnw test` |
| Full | Tasks com `*IT` | `./mvnw verify` |
| Build | Migration, fim de fase | `./mvnw verify` |

---

## Execution Plan

### Phase 1: Domínio e casos de uso

```
T1 → T2 → T3 → T4 → T5
```

### Phase 2: Infraestrutura

```
T6 → T7 → T8
```

---

## Task Breakdown

### T1: Criar entidade Servico e ServicoRepository

**What**: `Servico` (id, nome obrigatório, valor padrão > 0, `alterar`) e interface `ServicoRepository` (salvar, buscarPorId, existePorNome ignorando caixa, listar paginado, remover).
**Where**: `src/main/java/br/com/fiap/oficina/domain/ordemservico/Servico.java`
**Depends on**: None
**Reuses**: `DomainException`, `Pagina`
**Requirement**: SRV-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `ServicoTest`: valor 0 e negativo rejeitados (SRV-01.2); nome vazio rejeitado; `alterar` aplica as mesmas regras
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T2: Criar caso de uso CadastrarServico

**What**: Cadastra serviço; `ConflitoException` se o nome existe ignorando caixa. DTOs `CadastrarServicoDTO` e `ServicoDTO`.
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/CadastrarServico.java`
**Depends on**: T1
**Reuses**: `ServicoRepository`
**Requirement**: SRV-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `CadastrarServicoTest` com `ServicoRepositoryEmMemoria`: sucesso (SRV-01.1), valor inválido (SRV-01.2), nome repetido em outra caixa (SRV-01.3)
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T3: Criar caso de uso AtualizarServico

**What**: Altera nome e valor; 409 se o novo nome pertence a outro serviço; 404 se não existe.
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/AtualizarServico.java`
**Depends on**: T2
**Reuses**: `ServicoRepositoryEmMemoria`
**Requirement**: SRV-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `AtualizarServicoTest`: alteração (SRV-01.4), nome de outro serviço → 409, inexistente → 404
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T4: Criar caso de uso ConsultarServicos

**What**: `buscarPorId` e `listar(pagina, tamanho)`.
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/ConsultarServicos.java`
**Depends on**: T3
**Reuses**: `ServicoRepositoryEmMemoria`
**Requirement**: SRV-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `ConsultarServicosTest`: listagem paginada (SRV-01.5), inexistente → 404
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T5: Criar caso de uso RemoverServico

**What**: Remove serviço; 404 se não existe (409 por OS vem da FK, escopo 5).
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/RemoverServico.java`
**Depends on**: T4
**Reuses**: `ServicoRepositoryEmMemoria`
**Requirement**: SRV-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `RemoverServicoTest`: removido; inexistente → 404
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T6: Criar migration de serviço

**What**: Tabela `servico` (nome, `valor_padrao numeric(12,2) check > 0`) e índice único em `lower(nome)`.
**Where**: `src/main/resources/db/migration/V2__servico.sql`
**Depends on**: None
**Reuses**: padrão de migrations do design
**Requirement**: SRV-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] Flyway aplica em `OficinaApplicationIT`
- [ ] Gate check passes: `./mvnw verify`

**Tests**: none
**Gate**: build

---

### T7: Criar persistência de Servico

**What**: `ServicoEntity`, `ServicoJpaRepository` e `ServicoRepositoryImpl`.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/db/ordemservico/ServicoRepositoryImpl.java`
**Depends on**: T6
**Reuses**: padrão de persistência do design
**Requirement**: SRV-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `ServicoRepositoryImplIT`: salvar, buscar, `existePorNome` ignorando caixa, listar, remover
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

---

### T8: Criar ServicoController

**What**: `POST/GET/PUT/DELETE /servicos`; registra casos de uso em `BeanConfig`.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/http/ordemservico/ServicoController.java`
**Depends on**: T7
**Reuses**: `ErroHandler`, `SecurityConfig`
**Requirement**: SRV-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `ServicoControllerIT` (com JWT): 201, 422, 409, alteração, listagem, 404, remoção, sem token 401
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full
