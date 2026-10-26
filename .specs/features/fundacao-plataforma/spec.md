# Fundação da Plataforma Specification

## Problem Statement

O repositório só tem `pom.xml` e pacotes vazios. Os requisitos técnicos obrigatórios exigem execução local com Docker, JWT nas APIs administrativas, documentação OpenAPI e cobertura mínima de 80%, base de todos os escopos.

## Goals

- [ ] `docker compose up` sobe aplicação + PostgreSQL e o Swagger responde.
- [ ] `./mvnw verify` roda ArchUnit, testes de integração e o gate JaCoCo de 80%.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Cadastro de funcionários, refresh token, perfis | Um usuário de demonstração atende o requisito |
| Notificações por e-mail | AD-003: sem item obrigatório |
| Serviços AWS | Próximas fases |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Credencial de demonstração | Um funcionário em memória lido de propriedades (senha com hash BCrypt), documentado no README | README exige credenciais de demonstração; sem tabela para um usuário | n |
| Validade do JWT | 60 minutos | Sem requisito; valor comum | n |
| Mapeamento de erros | 400 validação de request; 401 autenticação; 404 inexistente; 409 duplicidade ou recurso em uso; 422 `DomainException` | Contrato único para todos os escopos | n |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Execução local ⭐ MVP

**User Story**: Como avaliador, quero subir a solução com um comando.

**Why P1**: Dockerfile, docker-compose e execução local são obrigatórios.

**Acceptance Criteria**:

1. WHEN `docker compose up` é executado na raiz THEN o sistema SHALL subir aplicação e PostgreSQL e servir o Swagger UI com HTTP 200.
2. WHEN a aplicação inicia com banco vazio THEN o sistema SHALL aplicar as migrations Flyway antes de aceitar requisições.

**Independent Test**: Subir o compose em máquina limpa e abrir o Swagger.

---

### P1: Autenticação JWT ⭐ MVP

**User Story**: Como funcionário, quero obter um token para usar as APIs administrativas.

**Why P1**: Requisito obrigatório.

**Acceptance Criteria**:

1. WHEN o funcionário envia usuário e senha válidos ao endpoint de autenticação THEN o sistema SHALL responder 200 com JWT válido por 60 minutos.
2. IF usuário ou senha são inválidos THEN o sistema SHALL responder 401 sem token.
3. IF requisição à API administrativa chega sem token, com token expirado ou com assinatura inválida THEN o sistema SHALL responder 401.
4. WHEN a requisição chega em `/cliente/**`, no endpoint de autenticação ou na documentação OpenAPI THEN o sistema SHALL processá-la sem token (AD-001).

**Independent Test**: Endpoint administrativo sem token (401), autenticar, repetir com token (200).

---

### P1: Erros padronizados ⭐ MVP

**User Story**: Como consumidor da API, quero um formato único de erro.

**Why P1**: Todos os escopos usam o mesmo contrato.

**Acceptance Criteria**:

1. IF o request viola validação `@Valid` THEN o sistema SHALL responder 400 com `codigo`, `mensagem` e a lista `campos`.
2. IF o caso de uso lança `DomainException` THEN o sistema SHALL responder 422 com `codigo` e `mensagem`.
3. IF o recurso não existe THEN o sistema SHALL responder 404 com `codigo` e `mensagem`.
4. IF ocorre erro não tratado THEN o sistema SHALL responder 500 sem stack trace no corpo.

**Independent Test**: Integração provocando cada tipo de erro.

---

### P1: Guardrails de arquitetura e cobertura ⭐ MVP

**User Story**: Como time, quero que a build falhe quando camadas ou cobertura regridem.

**Why P1**: Organização em camadas e cobertura de 80% são obrigatórias.

**Acceptance Criteria**:

1. IF classe de `domain` ou `application` depende de `org.springframework` ou `jakarta.persistence` THEN o sistema SHALL falhar no teste ArchUnit.
2. IF `domain` depende de `application`/`infrastructure`, ou `application` depende de `infrastructure` THEN o sistema SHALL falhar no teste ArchUnit.
3. IF a cobertura de linhas de pacote de `domain` ou `application` fica abaixo de 80% THEN o sistema SHALL falhar `./mvnw verify`.

**Independent Test**: Dependência proibida em branch descartável faz `./mvnw test` falhar.

---

### P2: Documentação OpenAPI

**User Story**: Como avaliador, quero navegar pela API no Swagger.

**Why P2**: Obrigatório, cresce com cada escopo.

**Acceptance Criteria**:

1. WHEN o Swagger UI é aberto THEN o sistema SHALL listar os endpoints com esquema Bearer declarado nos administrativos.

**Independent Test**: Autenticar pelo Swagger UI.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| FUND-01 | P1: Execução local | Tasks | In Tasks |
| FUND-02 | P1: Autenticação JWT | Tasks | In Tasks |
| FUND-03 | P1: Erros padronizados | Tasks | In Tasks |
| FUND-04 | P1: Guardrails | Tasks | In Tasks |
| FUND-05 | P2: OpenAPI | Tasks | In Tasks |

**Coverage:** 5 total, 5 mapped tasks, 0 unmapped

---

## Success Criteria

- [ ] Máquina limpa sobe a solução e autentica seguindo só o README.
