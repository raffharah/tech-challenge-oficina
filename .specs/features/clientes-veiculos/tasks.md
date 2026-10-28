# Clientes e Veículos Tasks

## Execution Protocol (MANDATORY -- do not skip)

Implement these tasks with the `tlc-spec-driven` skill: **activate it by name and follow its Execute flow and Critical Rules.** Do not search for skill files by filesystem path. The skill is the source of truth for the full flow (per-task cycle, sub-agent delegation, adequacy review, Verifier, discrimination sensor).

**If the skill cannot be activated, STOP and tell the user - do not proceed without it.**

---

**Design**: `.specs/features/fundacao-plataforma/design.md` (padrões; escopo CRUD sem design próprio)
**Status**: Reviewed

---

## Test Coverage Matrix

> Same matrix as `fundacao-plataforma/tasks.md` (from `AGENTS.md` and `pom.xml`) - confirm before Execute.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Domain | unit | Todos os ramos; 1:1 com ACs | `src/test/java/br/com/fiap/oficina/domain/cliente/*Test.java` | `./mvnw test` |
| Application | unit | 1:1 com ACs e erros; repositórios em memória | `src/test/java/br/com/fiap/oficina/application/cliente/*Test.java` | `./mvnw test` |
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

### Phase 1: Domínio

```
T1 → T3
T2 → T4
```

### Phase 2: Casos de uso

```
T5 → T6 → T7 → T8
T9 → T10 → T11 → T12
```

### Phase 3: Infraestrutura

```
T13 → T14 → T15 → T16 → T17
```

---

## Task Breakdown

### T1: Criar objeto de valor CpfCnpj

**What**: Normaliza para dígitos e valida CPF (11) e CNPJ (14) por dígitos verificadores, rejeitando dígitos repetidos com `DomainException`.
**Where**: `src/main/java/br/com/fiap/oficina/domain/cliente/CpfCnpj.java`
**Depends on**: None
**Reuses**: `DomainException`
**Requirement**: CLI-01, CLI-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `CpfCnpjTest`: CPF e CNPJ válidos com e sem máscara; dígito verificador errado; todos dígitos iguais; tamanho inválido
- [x] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T2: Criar objeto de valor Placa

**What**: Normaliza (maiúscula, sem hífen) e aceita só ABC1234 e ABC1D23.
**Where**: `src/main/java/br/com/fiap/oficina/domain/cliente/Placa.java`
**Depends on**: None
**Reuses**: `DomainException`
**Requirement**: CLI-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `PlacaTest`: `abc-1234` → `ABC1234`; `ABC1D23` válida; `AB12345`, `ABCD123`, vazio rejeitados
- [x] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T3: Criar entidade Cliente e ClienteRepository

**What**: `Cliente` (id, nome obrigatório, `CpfCnpj`, `renomear`), interface `ClienteRepository` (salvar, buscarPorId, buscarPorCpfCnpj, listar paginado, remover) e `domain/Pagina`.
**Where**: `src/main/java/br/com/fiap/oficina/domain/cliente/Cliente.java`
**Depends on**: T1
**Reuses**: `CpfCnpj` (T1)
**Requirement**: CLI-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `ClienteTest`: nome vazio rejeitado; `renomear` mantém CpfCnpj (CLI-01.4)
- [x] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T4: Criar entidade Veiculo e VeiculoRepository

**What**: `Veiculo` (id, `Placa`, marca, modelo, ano validado entre 1900 e ano corrente + 1 recebido por parâmetro, clienteId, `atualizar`), interface `VeiculoRepository` (salvar, buscarPorId, buscarPorPlaca, listarPorCliente, existePorCliente, remover).
**Where**: `src/main/java/br/com/fiap/oficina/domain/cliente/Veiculo.java`
**Depends on**: T2
**Reuses**: `Placa` (T2)
**Requirement**: CLI-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `VeiculoTest`: ano 1899 e ano corrente + 2 rejeitados; 1900 e ano corrente + 1 aceitos (CLI-03.4); `atualizar` mantém placa e cliente (CLI-03.5)
- [x] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T5: Criar caso de uso CadastrarCliente

**What**: Cadastra cliente; `ConflitoException` se CPF/CNPJ já existe. DTOs `CadastrarClienteDTO` e `ClienteDTO`.
**Where**: `src/main/java/br/com/fiap/oficina/application/cliente/CadastrarCliente.java`
**Depends on**: None
**Reuses**: `ClienteRepository` (T3)
**Requirement**: CLI-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `CadastrarClienteTest` com `ClienteRepositoryEmMemoria`: sucesso (CLI-01.1), inválido (CLI-01.2), duplicado com máscara diferente (CLI-01.3)
- [x] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T6: Criar caso de uso AtualizarCliente

**What**: Altera o nome; `NaoEncontradoException` se não existe.
**Where**: `src/main/java/br/com/fiap/oficina/application/cliente/AtualizarCliente.java`
**Depends on**: T5
**Reuses**: `ClienteRepositoryEmMemoria` (T5)
**Requirement**: CLI-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `AtualizarClienteTest`: nome alterado e CpfCnpj mantido (CLI-01.4); id inexistente → 404
- [x] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T7: Criar caso de uso ConsultarClientes

**What**: `buscarPorId`, `buscarPorCpfCnpj` (aceita máscara) e `listar(pagina, tamanho)`.
**Where**: `src/main/java/br/com/fiap/oficina/application/cliente/ConsultarClientes.java`
**Depends on**: T6
**Reuses**: `ClienteRepositoryEmMemoria`
**Requirement**: CLI-01, CLI-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `ConsultarClientesTest`: busca com e sem máscara (CLI-02.1); inexistente → 404 (CLI-02.2); listagem paginada (CLI-01.5)
- [x] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T8: Criar caso de uso RemoverCliente

**What**: Remove cliente; `ConflitoException` se possui veículos; 404 se não existe.
**Where**: `src/main/java/br/com/fiap/oficina/application/cliente/RemoverCliente.java`
**Depends on**: T7
**Reuses**: `ClienteRepositoryEmMemoria`, `VeiculoRepository` (T4)
**Requirement**: CLI-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `RemoverClienteTest`: sem veículos removido; com veículo → 409 (CLI-01.6); inexistente → 404
- [x] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T9: Criar caso de uso CadastrarVeiculo

**What**: Cadastra veículo para cliente existente (404 se não), `ConflitoException` para placa repetida; ano corrente via `Clock`. DTOs `CadastrarVeiculoDTO` e `VeiculoDTO`.
**Where**: `src/main/java/br/com/fiap/oficina/application/cliente/CadastrarVeiculo.java`
**Depends on**: None
**Reuses**: `VeiculoRepository`, `ClienteRepository`
**Requirement**: CLI-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `CadastrarVeiculoTest` com `VeiculoRepositoryEmMemoria` e `Clock` fixo: sucesso normalizado (CLI-03.1), placa inválida (CLI-03.2), repetida (CLI-03.3), ano fora (CLI-03.4), cliente inexistente → 404
- [x] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T10: Criar caso de uso AtualizarVeiculo

**What**: Altera marca, modelo e ano; 404 se não existe.
**Where**: `src/main/java/br/com/fiap/oficina/application/cliente/AtualizarVeiculo.java`
**Depends on**: T9
**Reuses**: `VeiculoRepositoryEmMemoria` (T9)
**Requirement**: CLI-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `AtualizarVeiculoTest`: campos alterados, placa e cliente mantidos (CLI-03.5); inexistente → 404
- [x] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T11: Criar caso de uso ConsultarVeiculos

**What**: `buscarPorId`, `buscarPorPlaca` (aceita formato não normalizado) e `listarPorCliente`.
**Where**: `src/main/java/br/com/fiap/oficina/application/cliente/ConsultarVeiculos.java`
**Depends on**: T10
**Reuses**: `VeiculoRepositoryEmMemoria`
**Requirement**: CLI-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `ConsultarVeiculosTest`: `abc-1234` encontra `ABC1234`; listagem só do cliente (CLI-03.6); inexistente → 404
- [x] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T12: Criar caso de uso RemoverVeiculo

**What**: Remove veículo; 404 se não existe (409 por OS vem da FK, escopo 5).
**Where**: `src/main/java/br/com/fiap/oficina/application/cliente/RemoverVeiculo.java`
**Depends on**: T11
**Reuses**: `VeiculoRepositoryEmMemoria`
**Requirement**: CLI-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `RemoverVeiculoTest`: removido; inexistente → 404
- [x] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T13: Criar migration de cliente e veículo

**What**: Tabelas `cliente` (cpf_cnpj único) e `veiculo` (placa única, FK `cliente_id`).
**Where**: `src/main/resources/db/migration/V1__cliente_veiculo.sql`
**Depends on**: None
**Reuses**: padrão de migrations do design
**Requirement**: CLI-01, CLI-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] Flyway aplica em `OficinaApplicationIT`
- [x] Gate check passes: `./mvnw verify`

**Tests**: none
**Gate**: build

---

### T14: Criar persistência de Cliente

**What**: `ClienteEntity`, `ClienteJpaRepository` e `ClienteRepositoryImpl` convertendo modelo ↔ entity.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/db/cliente/ClienteRepositoryImpl.java`
**Depends on**: T13
**Reuses**: padrão de persistência do design
**Requirement**: CLI-01, CLI-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `ClienteRepositoryImplIT`: salvar/buscar por id e CPF/CNPJ, listar paginado, remover; CPF/CNPJ duplicado viola constraint
- [x] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

---

### T15: Criar persistência de Veiculo

**What**: `VeiculoEntity`, `VeiculoJpaRepository` e `VeiculoRepositoryImpl`.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/db/cliente/VeiculoRepositoryImpl.java`
**Depends on**: T14
**Reuses**: padrão de T14
**Requirement**: CLI-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `VeiculoRepositoryImplIT`: buscar por placa, listar por cliente, existe por cliente, remover
- [x] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

---

### T16: Criar ClienteController

**What**: `POST/GET/PUT/DELETE /clientes`, `GET /clientes?cpfCnpj=`, requests com `@Valid`, `@Transactional`; registra casos de uso de cliente em `BeanConfig`.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/http/cliente/ClienteController.java`
**Depends on**: T15
**Reuses**: `ErroHandler`, `SecurityConfig`
**Requirement**: CLI-01, CLI-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `ClienteControllerIT` (com JWT): 201, 422, 409, atualização, listagem, busca por CPF/CNPJ com máscara, 404, remoção com veículo → 409, sem token → 401
- [x] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

---

### T17: Criar VeiculoController

**What**: `POST/GET/PUT/DELETE /veiculos`, `GET /veiculos?placa=`, `GET /clientes/{id}/veiculos`; registra casos de uso em `BeanConfig`.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/http/cliente/VeiculoController.java`
**Depends on**: T16
**Reuses**: padrão de T16
**Requirement**: CLI-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `VeiculoControllerIT` (com JWT): 201 normalizado, placa inválida 422, repetida 409, ano fora 422, atualização, listagem por cliente, 404, sem token 401
- [x] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full
