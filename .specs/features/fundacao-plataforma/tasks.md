# Fundação da Plataforma Tasks

## Execution Protocol (MANDATORY -- do not skip)

Implement these tasks with the `tlc-spec-driven` skill: **activate it by name and follow its Execute flow and Critical Rules.** Do not search for skill files by filesystem path. The skill is the source of truth for the full flow (per-task cycle, sub-agent delegation, adequacy review, Verifier, discrimination sensor).

**If the skill cannot be activated, STOP and tell the user - do not proceed without it.**

---

**Design**: `.specs/features/fundacao-plataforma/design.md`
**Status**: Reviewed

---

## Test Coverage Matrix

> Generated from `AGENTS.md` (seção Testes, TDD e F.I.R.S.T.) e `pom.xml` (surefire/failsafe/JaCoCo) - confirm before Execute. Repositório sem testes existentes.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Domain | unit | Todos os ramos; 1:1 com ACs; sem Spring | `src/test/java/br/com/fiap/oficina/domain/**/*Test.java` | `./mvnw test` |
| Application | unit | 1:1 com ACs e caminhos de erro; repositórios/portas em memória | `src/test/java/br/com/fiap/oficina/application/**/*Test.java` | `./mvnw test` |
| Controller / RepositoryImpl / Gateway / Config | integration | Cada rota: sucesso + bordas + erros; PostgreSQL via Testcontainers | `src/test/java/br/com/fiap/oficina/infrastructure/**/*IT.java` | `./mvnw verify` |
| Arquitetura | arch | Regras de camadas e contextos | `src/test/java/br/com/fiap/oficina/ArquiteturaTest.java` | `./mvnw test` |
| Migration / Dockerfile / compose / pom | none | build gate | - | `./mvnw verify` |

## Gate Check Commands

> Generated from `pom.xml` - confirm before Execute. Sem linter/formatter configurado.

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | Tasks só com testes unitários/arch | `./mvnw test` |
| Full | Tasks com `*IT` | `./mvnw verify` |
| Build | Fim de fase, tasks sem teste | `./mvnw verify` |

---

## Execution Plan

### Phase 1: Bootstrap

```
T1 → T2 → T3
```

### Phase 2: Guardrails e erros

```
T4 → T5 → T6
```

### Phase 3: Segurança e documentação

```
T7 → T8 → T9
```

---

## Task Breakdown

### T1: Criar aplicação e configuração base

**What**: `OficinaApplication`, `application.yml` (datasource por variáveis de ambiente, Flyway, `ddl-auto: validate`, propriedades `oficina.*`) e `TestcontainersConfig` de teste.
**Where**: `src/main/java/br/com/fiap/oficina/OficinaApplication.java`
**Depends on**: None
**Reuses**: autoconfiguração Spring Boot; `spring-boot-testcontainers`
**Requirement**: FUND-01

**Tools**:
- MCP: NONE
- Skill: `find-docs` (Testcontainers `@ServiceConnection` no Spring Boot 4)

**Done when**:
- [ ] `OficinaApplicationIT` sobe o contexto com PostgreSQL do Testcontainers e Flyway aplicado
- [ ] `OficinaApplicationIT` executa em `./mvnw verify` (confirma failsafe ativo)
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

---

### T2: Criar Dockerfile

**What**: Build multi-stage (`./mvnw package -DskipTests` em JDK 21, runtime JRE 21, usuário não root).
**Where**: `Dockerfile`
**Depends on**: T1
**Reuses**: Maven Wrapper existente
**Requirement**: FUND-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `docker build .` conclui sem erro
- [ ] Gate check passes: `./mvnw verify`

**Tests**: none
**Gate**: build

---

### T3: Criar docker-compose

**What**: Serviços `app` e `postgres` com healthcheck no banco, `depends_on: condition: service_healthy` e variáveis `DB_*`/`JWT_SEGREDO`.
**Where**: `docker-compose.yml`
**Depends on**: T2
**Reuses**: Dockerfile (T2)
**Requirement**: FUND-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `docker compose up -d` e `curl -f localhost:8080/swagger-ui.html` retornam sucesso (FUND-01.1)
- [ ] Gate check passes: `./mvnw verify`

**Tests**: none
**Gate**: build

---

### T4: Criar testes de arquitetura

**What**: Regras ArchUnit: `domain`/`application` sem `org.springframework` e `jakarta.persistence`; direção `infrastructure → application → domain`; contexto não depende de outro contexto em `domain`/`application`; todas com `allowEmptyShould(true)`.
**Where**: `src/test/java/br/com/fiap/oficina/ArquiteturaTest.java`
**Depends on**: None
**Reuses**: `archunit-junit5`
**Requirement**: FUND-04

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] Uma regra por AC FUND-04.1 e FUND-04.2, mais a regra de isolamento de contextos de `AGENTS.md`
- [ ] Classe temporária violando cada regra faz o teste falhar (removida antes do commit)
- [ ] Gate check passes: `./mvnw test`

**Tests**: arch
**Gate**: quick

---

### T5: Comprovar gate de cobertura JaCoCo

**What**: Confirmar que a regra `PACKAGE` do JaCoCo falha com pacote de `domain` abaixo de 80%; corrigir o padrão de `includes` se não falhar.
**Where**: `pom.xml`
**Depends on**: T4
**Reuses**: execução `check` existente (`pom.xml:131-158`)
**Requirement**: FUND-04

**Tools**:
- MCP: NONE
- Skill: `find-docs` (JaCoCo check rules)

**Done when**:
- [ ] Classe temporária sem teste em `domain/cliente` faz `./mvnw verify` falhar no `jacoco:check` (removida antes do commit)
- [ ] Gate check passes: `./mvnw verify`

**Tests**: none
**Gate**: build

---

### T6: Criar exceções de domínio e ErroHandler

**What**: `DomainException` (codigo, mensagem, detalhes), `NaoEncontradoException`, `ConflitoException`, `ErroResponse` e `ErroHandler` com o mapeamento do design.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/http/ErroHandler.java`
**Depends on**: T5
**Reuses**: design "ErroHandler"
**Requirement**: FUND-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `ErroHandlerIT` com controller de teste (`src/test`) cobre 400 com `campos`, 404, 409 (`ConflitoException` e `DataIntegrityViolationException`), 422 com `detalhes` e 500 sem stack trace
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

---

### T7: Configurar segurança JWT

**What**: `SecurityConfig` com filter chain stateless, rotas públicas, resource server JWT HS256, `JwtEncoder`, `InMemoryUserDetailsManager` e `PasswordEncoder`.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/config/SecurityConfig.java`
**Depends on**: None (Phase 2 concluída)
**Reuses**: `spring-boot-starter-security-oauth2-resource-server`
**Requirement**: FUND-02

**Tools**:
- MCP: NONE
- Skill: `find-docs` (Spring Security 7 resource server com chave simétrica)

**Done when**:
- [ ] `SecurityConfigIT`: rota administrativa sem token → 401; token com assinatura inválida → 401; token expirado → 401 (FUND-02.3); `/cliente/**` e `/v3/api-docs` sem token → não 401 (FUND-02.4)
- [ ] `ErroHandlerIT` (T6) continua verde, autenticado com `jwt()`
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

---

### T8: Criar endpoint de autenticação

**What**: `POST /autenticacao` valida credenciais pelo `AuthenticationManager` e emite JWT de 60 minutos com `Clock`; registra `Clock` em `BeanConfig`.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/http/autenticacao/AutenticacaoController.java`
**Depends on**: T7
**Reuses**: `JwtEncoder` e `UserDetailsService` (T7)
**Requirement**: FUND-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `AutenticacaoIT`: credencial válida → 200 com token aceito em rota administrativa e `exp` = emissão + 60 min (FUND-02.1); credencial inválida → 401 sem token (FUND-02.2)
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

---

### T9: Declarar esquema Bearer no OpenAPI

**What**: `OpenApiConfig` com `@SecurityScheme` bearer JWT e informações da API.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/config/OpenApiConfig.java`
**Depends on**: T8
**Reuses**: springdoc
**Requirement**: FUND-05

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `OpenApiIT`: `GET /v3/api-docs` → 200 com `components.securitySchemes.bearer`; `GET /swagger-ui.html` → 200 sem token
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full
