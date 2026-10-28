# Validação: Fundação da Plataforma

## Validation

**Result**: PASS

Verificação inline pelo orquestrador (sem sub-agent, a pedido do usuário). Autor e verificador são a mesma sessão: independência parcial, compensada pelo sensor de discriminação em cópia isolada.

Diff: working tree sobre `main` (sem commits, por instrução do usuário).

## Gate de build

`./mvnw -B verify` (JAVA_HOME=JDK 21): BUILD SUCCESS.

- Testes: 23 (5 `ArquiteturaTest` + 18 `*IT`), 0 falhas, 0 skips.
- JaCoCo: "All coverage checks have been met" (regra 0.80 em `pom.xml:150`).
- Prova de falha do gate (T4/T9): classe sem teste em `domain/cliente` gerou "lines covered ratio is 0.00, but expected minimum is 0.80".

## Critérios de aceitação

| AC | Resultado esperado | Evidência | Resultado |
| --- | --- | --- | --- |
| FUND-01 compose sobe app + PostgreSQL, Swagger responde | Swagger 200 após `docker compose up` | `docker-compose.yml:10` healthcheck, `docker-compose.yml:27` `service_healthy`; execução manual: swagger 200, `/autenticacao` devolve token | PASS |
| FUND-02.1 credencial válida → 200, JWT 60 min | 200, `exp - iat` = 60 min | `AutenticacaoIT.java:49` `isEqualTo(Duration.ofMinutes(60))` | PASS |
| FUND-02.2 credencial inválida → 401 sem token | 401, corpo vazio | `AutenticacaoIT.java:59-60`, `AutenticacaoIT.java:67-68` | PASS |
| FUND-02.3 sem token / expirado / assinatura inválida → 401 | 401 | `SecurityConfigIT.java:41`, `SecurityConfigIT.java:49`, `SecurityConfigIT.java:57` | PASS |
| FUND-02.4 `/cliente/**`, autenticação, OpenAPI sem token | 200 | `SecurityConfigIT.java:71` (cliente), `SecurityConfigIT.java:77` (api-docs), `AutenticacaoIT.java:42` (autenticação) | PASS |
| FUND-03.1 `@Valid` → 400 com `codigo`, `mensagem`, `campos` | 400, `REQUISICAO_INVALIDA`, `campos[0].campo` = `nome` | `ErroHandlerIT.java:45-49` | PASS |
| FUND-03.2 `DomainException` → 422 | 422, `SALDO_INSUFICIENTE`, mensagem | `ErroHandlerIT.java:79-82` | PASS |
| FUND-03.3 inexistente → 404 | 404, `CLIENTE_NAO_ENCONTRADO` | `ErroHandlerIT.java:55-57` | PASS |
| FUND-03.4 erro não tratado → 500 sem stack trace | 500, corpo sem `stackTrace` / `at br.com` | `ErroHandlerIT.java:88-93` | PASS |
| FUND-03 (409 duplicidade/recurso em uso, premissa do spec) | 409 | `ErroHandlerIT.java:63-65`, `ErroHandlerIT.java:71-73` | PASS |
| FUND-04.1 domain/application dependem de Spring/JPA → ArchUnit falha | regra ativa | `ArquiteturaTest.java:19-21`; prova com classe violadora temporária, removida | PASS |
| FUND-04.2 dependência entre camadas / contextos → ArchUnit falha | regra ativa | `ArquiteturaTest.java:24-43` | PASS |
| FUND-04.3 pacote abaixo de 80% → `verify` falha | falha de build | `pom.xml:149-150`; prova de 0.00 acima | PASS |
| FUND-05 Swagger lista endpoints com esquema Bearer | `securitySchemes.bearer` http/bearer; UI 200 | `OpenApiIT.java:30-32`, `OpenApiIT.java:38` | PASS |

Lacuna de precisão no spec: ACs de FUND-01 e FUND-04.1/04.2 não definem valor numérico observável. `allowEmptyShould(true)` mantém as regras ArchUnit ativas sem classes em `domain`/`application`; a prova por violador temporário cobre o discriminador.

## Sensor de discriminação

Cópia em diretório temporário (`rsync`, sem `.git`); árvore real intocada (`git status --porcelain` idêntico ao baseline).

| Mutante | Arquivo | Resultado |
| --- | --- | --- |
| `/cliente/**` deixa de ser público | `SecurityConfig.java:38` | morto: `SecurityConfigIT.rotaDeClienteSemTokenEPublica` |
| `UNPROCESSABLE_ENTITY` → `BAD_REQUEST` | `ErroHandler.java:50` | morto: `ErroHandlerIT.regraDeNegocioRetorna422ComDetalhes` |
| validade do JWT 60 → 30 min | `AutenticacaoController.java:24` | morto: `AutenticacaoIT.credencialValidaRetorna200ComTokenValidoPor60Minutos` |

3 de 3 mortos, 0 sobreviventes.

## Qualidade

- Escopo: só arquivos das tarefas; `.dockerignore` é extra (necessário para build do Dockerfile sem enviar `target/`).
- Padrões do AGENTS.md respeitados: exceções de domínio em `domain/`, handler em `infrastructure/http/`, segurança em `infrastructure/config/`, ITs com Testcontainers.
- Fora de escopo, pendente: `README.md` ainda afirma que segurança e credenciais não estão implementadas.
