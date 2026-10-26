# Estoque de Peças e Insumos Tasks

## Execution Protocol (MANDATORY -- do not skip)

Implement these tasks with the `tlc-spec-driven` skill: **activate it by name and follow its Execute flow and Critical Rules.** Do not search for skill files by filesystem path. The skill is the source of truth for the full flow (per-task cycle, sub-agent delegation, adequacy review, Verifier, discrimination sensor).

**If the skill cannot be activated, STOP and tell the user - do not proceed without it.**

---

**Design**: `.specs/features/fundacao-plataforma/design.md` (padrões) e `.specs/features/abertura-os-orcamento/design.md` (porta `EstoqueDeItens`, concorrência na baixa)
**Status**: Draft

---

## Test Coverage Matrix

> Same matrix as `fundacao-plataforma/tasks.md` - confirm before Execute.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Domain | unit | Todos os ramos; 1:1 com ACs | `src/test/java/br/com/fiap/oficina/domain/estoque/*Test.java` | `./mvnw test` |
| Application | unit | 1:1 com ACs e erros; repositório em memória | `src/test/java/br/com/fiap/oficina/application/estoque/*Test.java` | `./mvnw test` |
| Controller / RepositoryImpl | integration | Cada rota: sucesso + bordas + erros; concorrência na baixa | `src/test/java/br/com/fiap/oficina/infrastructure/**/*IT.java` | `./mvnw verify` |
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
T1 → T2 → T3 → T4 → T5 → T6 → T7
```

### Phase 2: Infraestrutura

```
T8 → T9 → T10
```

---

## Task Breakdown

### T1: Criar entidade ItemEstoque e ItemEstoqueRepository

**What**: `TipoItem` (PECA, INSUMO), `ItemEstoque` (id, sku, nome, tipo, preço de venda > 0, saldo físico ≥ 0, `atualizar`, `registrarEntrada(q > 0)`, `baixar(q)` que rejeita saldo insuficiente) e interface `ItemEstoqueRepository` (salvar, buscarPorId, buscarPorIds, existePorSku, listar, remover).
**Where**: `src/main/java/br/com/fiap/oficina/domain/estoque/ItemEstoque.java`
**Depends on**: None
**Reuses**: `DomainException`, `Pagina`
**Requirement**: EST-01, EST-02, EST-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `ItemEstoqueTest`: preço ≤ 0 e saldo inicial negativo rejeitados (EST-01.3); `atualizar` não muda saldo (EST-01.4); entrada soma e rejeita ≤ 0 (EST-02); baixa exata zera; baixa acima do saldo rejeitada sem alterar (EST-03.3)
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T2: Criar caso de uso CadastrarItem

**What**: Cadastra item com saldo inicial; `ConflitoException` se SKU existe. DTOs `CadastrarItemDTO` e `ItemEstoqueDTO`.
**Where**: `src/main/java/br/com/fiap/oficina/application/estoque/CadastrarItem.java`
**Depends on**: T1
**Reuses**: `ItemEstoqueRepository`
**Requirement**: EST-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `CadastrarItemTest` com `ItemEstoqueRepositoryEmMemoria`: sucesso com saldo físico = inicial (EST-01.1), SKU repetido (EST-01.2), preço inválido (EST-01.3)
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T3: Criar caso de uso AtualizarItem

**What**: Altera nome e preço de venda; 404 se não existe.
**Where**: `src/main/java/br/com/fiap/oficina/application/estoque/AtualizarItem.java`
**Depends on**: T2
**Reuses**: `ItemEstoqueRepositoryEmMemoria`
**Requirement**: EST-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `AtualizarItemTest`: alteração sem mudar saldo (EST-01.4), inexistente → 404
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T4: Criar caso de uso ConsultarItens

**What**: `buscarPorId` e `listar(pagina, tamanho)` com tipo, preço e saldo físico.
**Where**: `src/main/java/br/com/fiap/oficina/application/estoque/ConsultarItens.java`
**Depends on**: T3
**Reuses**: `ItemEstoqueRepositoryEmMemoria`
**Requirement**: EST-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `ConsultarItensTest`: listagem e busca retornam tipo, preço e saldo (EST-01.5); inexistente → 404
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T5: Criar caso de uso RemoverItem

**What**: Remove item; 404 se não existe (409 por OS vem da FK, escopo 5).
**Where**: `src/main/java/br/com/fiap/oficina/application/estoque/RemoverItem.java`
**Depends on**: T4
**Reuses**: `ItemEstoqueRepositoryEmMemoria`
**Requirement**: EST-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `RemoverItemTest`: removido; inexistente → 404
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T6: Criar caso de uso RegistrarEntrada

**What**: Soma quantidade ao saldo físico do item; 404 se não existe.
**Where**: `src/main/java/br/com/fiap/oficina/application/estoque/RegistrarEntrada.java`
**Depends on**: T5
**Reuses**: `ItemEstoqueRepositoryEmMemoria`
**Requirement**: EST-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `RegistrarEntradaTest`: 10 + 5 = 15 (EST-02.1); quantidade 0 → 422 (EST-02.2); inexistente → 404
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T7: Criar caso de uso BaixarItens

**What**: Recebe lista (itemId, quantidade), verifica todos antes de alterar e baixa tudo ou nada; falta → `DomainException` com `detalhes` (sku, quantidade pedida, saldo físico) de cada faltante.
**Where**: `src/main/java/br/com/fiap/oficina/application/estoque/BaixarItens.java`
**Depends on**: T6
**Reuses**: `ItemEstoque.baixar` (T1)
**Requirement**: EST-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `BaixarItensTest`: todos com saldo baixados (EST-03.1); um faltante → nenhum saldo alterado e `detalhes` lista só o faltante (EST-03.2); item inexistente → 404
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T8: Criar migration de item de estoque

**What**: Tabela `item_estoque` (sku único, tipo `check in ('PECA','INSUMO')`, `preco_venda numeric(12,2) check > 0`, `saldo_fisico int check >= 0`, `versao bigint`).
**Where**: `src/main/resources/db/migration/V3__item_estoque.sql`
**Depends on**: None
**Reuses**: padrão de migrations do design
**Requirement**: EST-01, EST-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] Flyway aplica em `OficinaApplicationIT`
- [ ] Gate check passes: `./mvnw verify`

**Tests**: none
**Gate**: build

---

### T9: Criar persistência de ItemEstoque

**What**: `ItemEstoqueEntity` (com `@Version`), `ItemEstoqueJpaRepository` e `ItemEstoqueRepositoryImpl` atualizando a entity gerenciada.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/db/estoque/ItemEstoqueRepositoryImpl.java`
**Depends on**: T8
**Reuses**: padrão de persistência do design
**Requirement**: EST-01, EST-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `ItemEstoqueRepositoryImplIT`: CRUD, `buscarPorIds`, SKU duplicado viola constraint
- [ ] Duas transações concorrentes baixando o último item: uma confirma, a outra falha com `ObjectOptimisticLockingFailureException`; saldo final 0
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

---

### T10: Criar ItemEstoqueController

**What**: `POST/GET/PUT/DELETE /itens` e `POST /itens/{id}/entradas`; registra casos de uso em `BeanConfig`.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/http/estoque/ItemEstoqueController.java`
**Depends on**: T9
**Reuses**: `ErroHandler`, `SecurityConfig`
**Requirement**: EST-01, EST-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `ItemEstoqueControllerIT` (com JWT): 201, SKU 409, preço 422, alteração, listagem, entrada soma saldo, entrada 0 → 422, 404, sem token 401
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full
