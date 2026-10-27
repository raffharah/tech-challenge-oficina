# Acompanhamento da OS Tasks

## Execution Protocol (MANDATORY -- do not skip)

Implement these tasks with the `tlc-spec-driven` skill: **activate it by name and follow its Execute flow and Critical Rules.** Do not search for skill files by filesystem path. The skill is the source of truth for the full flow (per-task cycle, sub-agent delegation, adequacy review, Verifier, discrimination sensor).

**If the skill cannot be activated, STOP and tell the user - do not proceed without it.**

---

**Design**: `.specs/features/abertura-os-orcamento/design.md` (agregado OS, endpoints, risco do tempo médio)
**Status**: Reviewed

---

## Test Coverage Matrix

> Same matrix as `fundacao-plataforma/tasks.md` - confirm before Execute.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Domain | unit | Todos os ramos; 1:1 com ACs | `src/test/java/br/com/fiap/oficina/domain/ordemservico/*Test.java` | `./mvnw test` |
| Application | unit | 1:1 com ACs e erros | `src/test/java/br/com/fiap/oficina/application/ordemservico/*Test.java` | `./mvnw test` |
| Controller / RepositoryImpl / Gateway | integration | Cada rota: sucesso + bordas + erros | `src/test/java/br/com/fiap/oficina/infrastructure/**/*IT.java` | `./mvnw verify` |

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
T1 → T5
T2 → T3 → T4 → T5
```

### Phase 2: Infraestrutura

```
T6 → T7 → T8 → T9
```

---

## Task Breakdown

### T1: Adicionar tempo de execução à OrdemServico

**What**: `tempoDeExecucao()` devolve `Duration` entre a mudança para EM_EXECUCAO e a mudança para FINALIZADA do histórico, vazio se não finalizada.
**Where**: `src/main/java/br/com/fiap/oficina/domain/ordemservico/OrdemServico.java` (modify)
**Depends on**: None
**Reuses**: histórico de status (abertura T3)
**Requirement**: ACO-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `OrdemServicoTest`: OS finalizada 60 min após aprovação → 60 min; OS não finalizada → vazio
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T2: Criar caso de uso ConsultarAndamento

**What**: Normaliza o CPF/CNPJ pela porta `CadastroDeVeiculos.normalizarCpfCnpj` e devolve a OS só se pertencer ao titular; OS inexistente e titular diferente lançam a mesma `NaoEncontradoException`.
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/ConsultarAndamento.java`
**Depends on**: None
**Reuses**: `OrdemServicoDTO`, `CadastroDeVeiculos` (abertura T6)
**Requirement**: ACO-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `ConsultarAndamentoTest`: titular recebe status, histórico, componentes e total (ACO-01.1); outro CPF e OS inexistente → mesma mensagem 404 (ACO-01.2)
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T3: Criar caso de uso ListarOrdensServico

**What**: Lista paginada por data de abertura decrescente com número, CPF/CNPJ, placa, status e total; acrescenta `listar` ao `OrdemServicoRepository`.
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/ListarOrdensServico.java`
**Depends on**: T2
**Reuses**: `Pagina`, `OrdemServicoRepositoryEmMemoria`
**Requirement**: ACO-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `ListarOrdensServicoTest`: duas OS retornadas da mais nova para a mais antiga com os campos de ACO-02.1
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T4: Criar caso de uso DetalharOrdemServico

**What**: Devolve `OrdemServicoDTO` completo; 404 se não existe.
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/DetalharOrdemServico.java`
**Depends on**: T3
**Reuses**: `OrdemServicoDTO`
**Requirement**: ACO-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `DetalharOrdemServicoTest`: CPF/CNPJ, placa, componentes, total e histórico (ACO-02.2); inexistente → 404 (ACO-02.3)
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T5: Criar caso de uso CalcularTempoMedioExecucao

**What**: Média em minutos de `tempoDeExecucao()` das OS de `listarFinalizadas()` (inclui ENTREGUE) e quantidade; acrescenta `listarFinalizadas` ao repositório; comentário `ponytail:` com o teto de memória.
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/CalcularTempoMedioExecucao.java`
**Depends on**: T1, T4
**Reuses**: `OrdemServico.tempoDeExecucao` (T1)
**Requirement**: ACO-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `CalcularTempoMedioExecucaoTest`: 60 e 120 min → média 90, quantidade 2 (ACO-03.1); nenhuma → quantidade 0 e média nula (ACO-03.2)
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T6: Implementar listagens no repositório da OS

**What**: `listar(pagina, tamanho)` ordenado por `aberta_em desc` e `listarFinalizadas()` (status FINALIZADA ou ENTREGUE) no `OrdemServicoRepositoryImpl`.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/db/ordemservico/OrdemServicoRepositoryImpl.java` (modify)
**Depends on**: None
**Reuses**: `OrdemServicoJpaRepository` (abertura T13)
**Requirement**: ACO-02, ACO-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `OrdemServicoRepositoryImplIT`: ordem decrescente e paginação; `listarFinalizadas` ignora OS em outros status
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

---

### T7: Implementar CadastroDeVeiculos.normalizarCpfCnpj

**What**: Normaliza com o objeto de valor `CpfCnpj` do contexto cliente, lançando 422 para formato inválido.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/gateway/cliente/CadastroDeVeiculosImpl.java` (modify)
**Depends on**: T6
**Reuses**: `CpfCnpj` (clientes T1)
**Requirement**: ACO-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `CadastroDeVeiculosImplIT`: CPF mascarado → só dígitos; inválido → `DomainException`
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

---

### T8: Criar endpoints administrativos de acompanhamento

**What**: `GET /ordens-servico`, `GET /ordens-servico/{id}` e `GET /ordens-servico/tempo-medio-execucao`; registra os casos de uso em `BeanConfig`.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/http/ordemservico/OrdemServicoController.java` (modify)
**Depends on**: T7
**Reuses**: padrão do `OrdemServicoController`
**Requirement**: ACO-02, ACO-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `AcompanhamentoAdminIT` (com JWT): listagem ordenada; detalhe; 404; tempo médio com duas OS finalizadas em `Clock` controlado; sem OS → quantidade 0; sem token 401
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

---

### T9: Criar endpoint de consulta do cliente

**What**: `GET /cliente/ordens-servico/{id}?cpfCnpj=` no `ClienteOrdemServicoController` (sem JWT); registra `ConsultarAndamento` em `BeanConfig`.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/http/ordemservico/ClienteOrdemServicoController.java` (modify)
**Depends on**: T8
**Reuses**: `ClienteOrdemServicoController` (decisão T5)
**Requirement**: ACO-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `ConsultaClienteIT` (sem token): titular com CPF mascarado → 200 com status, histórico, componentes e total; outro CPF e OS inexistente → 404 com corpo idêntico
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full
