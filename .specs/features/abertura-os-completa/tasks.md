# Abertura de OS Completa Tasks

## Execution Protocol (MANDATORY -- do not skip)

Implement these tasks with the `tlc-spec-driven` skill: **activate it by name and follow its Execute flow and Critical Rules.** Do not search for skill files by filesystem path. The skill is the source of truth for the full flow (per-task cycle, sub-agent delegation, adequacy review, Verifier, discrimination sensor).

**If the skill cannot be activated, STOP and tell the user - do not proceed without it.**

---

**Design**: `.specs/features/abertura-os-completa/design.md`
**Status**: Draft

---

## Test Coverage Matrix

Guidelines found: `AGENTS.md` (TDD, F.I.R.S.T., `*Test`/`*IT`, JaCoCo 80% em `domain` e `application`), `pom.xml` (failsafe, jacoco).

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Domain | unit | Todos os ramos; 1:1 com ACs | `src/test/java/br/com/fiap/oficina/domain/**/*Test.java` | `./mvnw test` |
| Application | unit | 1:1 com ACs e erros; repositórios e portas em memória | `src/test/java/br/com/fiap/oficina/application/**/*Test.java` | `./mvnw test` |
| Repository / Gateway | integration | Mapeamento do campo novo e chamada entre contextos | `src/test/java/br/com/fiap/oficina/infrastructure/{db,gateway}/**/*IT.java` | `./mvnw verify` |
| Controller | integration | Sucesso + bordas + erros; rollback sem resíduo | `src/test/java/br/com/fiap/oficina/infrastructure/http/**/*IT.java` | `./mvnw verify` |
| Docs | none | - | - | build gate only |

## Gate Check Commands

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | Tasks de domain/application | `./mvnw test` |
| Full | Tasks com `*IT` | `./mvnw verify` |
| Build | Fim de fase ou task sem teste | `./mvnw verify` |

---

## Execution Plan

### Phase 1: E-mail do cliente

```
T1 → T2 → T3 → T4
```

### Phase 2: Abertura no domínio e no caso de uso

```
T5 → T6 → T7
```

### Phase 3: API e documentação

```
T8 → T9
```

---

## Task Breakdown

### T1: Criar o objeto de valor Email

**What**: `Email` valida o formato depois de `trim()`, guarda em minúsculas e lança `DomainException("EMAIL_INVALIDO")` para valor vazio ou mal formado.
**Where**: `src/main/java/br/com/fiap/oficina/domain/cliente/Email.java`
**Depends on**: None
**Reuses**: padrão de `CpfCnpj` e `Placa`
**Requirement**: ABR-02, ABR-04

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `EmailTest`: `" Ana@Oficina.com "` vira `ana@oficina.com`; `ana`, `ana@`, `ana@oficina`, vazio e nulo lançam `EMAIL_INVALIDO` (ABR-04.4)
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

**Commit**: `feat(cliente): cria objeto de valor Email`

---

### T2: Registrar e-mail no Cliente

**What**: `Cliente` ganha o campo `email`, que pode ser nulo, recebido no construtor, e o método `registrarEmail(Email)`. Os chamadores de `new Cliente(...)` passam o e-mail atual (`null` no cadastro; o existente na atualização).
**Where**: `src/main/java/br/com/fiap/oficina/domain/cliente/Cliente.java` (modify)
**Depends on**: T1
**Reuses**: `Cliente.renomear`
**Requirement**: ABR-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `ClienteTest`: cliente novo sem e-mail; `registrarEmail` substitui o anterior
- [ ] `AtualizarClienteTest`: renomear mantém o e-mail
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

**Commit**: `feat(cliente): registra e-mail no cliente`

---

### T3: Criar caso de uso RegistrarEmailDoCliente

**What**: `executar(cpfCnpj, email)` busca o cliente pelo CPF/CNPJ (404 `CLIENTE_NAO_ENCONTRADO`), registra o `Email` e salva. `ClienteDTO` ganha `email`.
**Where**: `src/main/java/br/com/fiap/oficina/application/cliente/RegistrarEmailDoCliente.java`
**Depends on**: T2
**Reuses**: `ClienteRepositoryEmMemoria`, `ConsultarClientes.buscarPorCpfCnpj`
**Requirement**: ABR-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `RegistrarEmailDoClienteTest`: grava e substitui o e-mail; CPF/CNPJ sem cadastro lança `NaoEncontradoException`; e-mail inválido lança `EMAIL_INVALIDO` sem salvar
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

**Commit**: `feat(cliente): cria caso de uso para registrar e-mail do cliente`

---

### T4: Persistir o e-mail do cliente

**What**: Migration `V5__cliente_email.sql` (`alter table cliente add column email varchar(254)`), mais `ClienteEntity.email` e o mapeamento em `ClienteRepositoryImpl`.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/db/cliente/ClienteRepositoryImpl.java` (modify)
**Depends on**: T3
**Reuses**: `ClienteRepositoryImplIT`
**Requirement**: ABR-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `ClienteRepositoryImplIT`: cliente salvo com e-mail volta com o e-mail; cliente sem e-mail volta com nulo
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

**Commit**: `feat(cliente): persiste e-mail do cliente`

---

### T5: Abrir OS com serviços e itens solicitados

**What**:
- Criar o record `ItemSolicitado(ItemCotado item, int quantidade)`.
- Trocar `OrdemServico.abrir(veiculo, agora)` por `abrir(veiculo, servicos, itens, agora)`.
- Extrair `adicionarServico` e `adicionarItem` privados, usados pela abertura e pelos métodos `incluir*` (estes depois de `exigirEmDiagnostico`).
- Migrar fixtures e testes que usam a assinatura antiga (lista em `design.md`).

**Where**: `src/main/java/br/com/fiap/oficina/domain/ordemservico/OrdemServico.java` (modify)
**Depends on**: None
**Reuses**: regras atuais de `incluirServico` e `incluirItem`
**Requirement**: ABR-01, ABR-04

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `OrdemServicoTest`:
  - abertura nasce `RECEBIDA` com serviços e itens, valor do serviço e preço do item copiados e `total` nulo (ABR-01.1, ABR-01.3, ABR-01.4);
  - item repetido soma quantidade (ABR-01.7);
  - serviço repetido lança `SERVICO_JA_INCLUIDO` (ABR-04.6);
  - quantidade 0 lança `QUANTIDADE_INVALIDA` (ABR-04.5);
  - `RECEBIDA` rejeita `incluir*` e `remover*` com `ORDEM_FORA_DE_DIAGNOSTICO` (ABR-01.6);
  - depois de `iniciarDiagnostico`, incluir e remover alteram os componentes da abertura (ABR-01.5).
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

**Commit**: `feat(ordemservico): abre OS com servicos e itens solicitados`

---

### T6: Registrar e-mail pela porta CadastroDeVeiculos

**What**: A porta `CadastroDeVeiculos` ganha `registrarEmail(cpfCnpj, email)`. `CadastroDeVeiculosImpl` implementa chamando `RegistrarEmailDoCliente`, e `CadastroDeVeiculosSoNormaliza`, em teste, registra a chamada.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/gateway/cliente/CadastroDeVeiculosImpl.java` (modify)
**Depends on**: T5
**Reuses**: `RegistrarEmailDoCliente` (T3)
**Requirement**: ABR-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `CadastroDeVeiculosImplIT`: `registrarEmail` grava o e-mail no cliente; e-mail inválido lança `EMAIL_INVALIDO`
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

**Commit**: `feat(ordemservico): registra e-mail do titular pela porta de cadastro`

---

### T7: Abrir OS completa no caso de uso

**What**: `AbrirOrdemServicoDTO(cpfCnpj, email, placa, servicos, itens)` com `ItemSolicitadoDTO(itemId, quantidade)`. `AbrirOrdemServico`:
1. identifica o veículo;
2. busca cada serviço (404 `SERVICO_NAO_ENCONTRADO`);
3. cota cada item;
4. abre a OS;
5. registra o e-mail;
6. salva.

**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/AbrirOrdemServico.java` (modify)
**Depends on**: T6
**Reuses**: `ServicoRepositoryEmMemoria`, `EstoqueDeItensEmMemoria`, `OrdemServicoRepositoryEmMemoria`
**Requirement**: ABR-01, ABR-02, ABR-03, ABR-04

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `AbrirOrdemServicoTest`:
  - abre com componentes e devolve `id` (ABR-01.1);
  - registra o e-mail do titular (ABR-01.2);
  - serviço inexistente lança `NaoEncontradoException` sem salvar OS nem registrar e-mail (ABR-04.3, ABR-04.8);
  - item inexistente idem.
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

**Commit**: `feat(ordemservico): abre OS com cliente, veiculo, servicos e itens`

---

### T8: Receber a abertura completa na API

**What**:
- `AbrirOrdemServicoRequest` com `cpfCnpj`, `email` e `placa` `@NotBlank`, `servicos` `@NotEmpty` e `itens` `@NotEmpty @Valid`, usando o record aninhado `ItemSolicitadoRequest`, com `@Schema`.
- `OrdemServicoController.abrir` converte para o DTO e tem o `@Operation` atualizado.
- `BeanConfig` ganha os parâmetros novos de `AbrirOrdemServico` e o bean `RegistrarEmailDoCliente`.
- Migrar o corpo da abertura nas ITs listadas em `design.md`.

**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/http/ordemservico/OrdemServicoController.java` (modify)
**Depends on**: None (Phase 2 concluída)
**Reuses**: `ErroHandler` (400, 404, 422)
**Requirement**: ABR-01, ABR-03, ABR-04

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `OrdemServicoAberturaIT`:
  - 201 com `id`, status `RECEBIDA`, componentes, e-mail gravado no cliente e saldo do item intacto;
  - placa de outro cliente responde 422;
  - CPF/CNPJ ou placa sem cadastro respondem 404;
  - serviço inexistente responde 404, sem OS gravada e com o e-mail do cliente inalterado (ABR-04.8);
  - `email` inválido responde 422 `EMAIL_INVALIDO`;
  - `email` ausente, `servicos: []` ou `itens` ausente respondem 400.
- [ ] Demais ITs de OS verdes com o corpo novo
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

**Commit**: `feat(ordemservico): recebe dados completos na abertura da OS`

---

### T9: Atualizar a documentação DDD

**What**:
- `docs/ddd/modelo-de-dominio.md`: `Email` no agregado Cliente e componentes da OS na abertura.
- `docs/ddd/linguagem-ubiqua.md`: termo "E-mail do cliente" e a revisão de "Início do diagnóstico" nas Decisões do MVP.

**Where**: `docs/ddd/modelo-de-dominio.md` (modify)
**Depends on**: T8
**Reuses**: AD-011
**Requirement**: ABR-01, ABR-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] Os dois documentos descrevem a abertura implementada
- [ ] Gate check passes: `./mvnw verify`

**Tests**: none
**Gate**: build

**Commit**: `docs(ddd): registra e-mail do cliente e componentes na abertura da OS`
