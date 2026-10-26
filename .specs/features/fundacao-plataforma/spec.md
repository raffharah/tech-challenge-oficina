# Fundação da Plataforma Specification

## Problem Statement

O repositório só tem `pom.xml` e pacotes vazios. Todos os escopos de negócio dependem de uma aplicação que sobe localmente com PostgreSQL, autentica o funcionário por JWT, trata erros de forma uniforme, registra notificações simuladas e impede que as regras de arquitetura e cobertura de `AGENTS.md` regridam.

## Goals

- [ ] `docker compose up` sobe aplicação + PostgreSQL e o Swagger responde.
- [ ] `./mvnw verify` roda ArchUnit, testes de integração com Testcontainers e o gate JaCoCo de 80% em `domain` e `application`.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Cadastro e gestão de funcionários | Usuário de demonstração basta para o MVP |
| Refresh token, logout, múltiplos perfis | Não exigidos na fase 1 |
| Envio real de e-mail (AWS SES ou SMTP) | Decisões do MVP: porta com implementação simulada |
| Serviços AWS | Entram nas próximas fases |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Credencial de demonstração | Um funcionário criado por migration, senha armazenada com hash BCrypt, documentada no README | Requisito pede credenciais demo no README | n |
| Validade do JWT | 60 minutos | Valor comum; não há requisito | n |
| Mapeamento de erros HTTP | 400 validação sintática; 401 sem token ou token inválido; 404 recurso inexistente; 409 duplicidade/conflito de referência; 422 `DomainException` | Separa validação de request (`@Valid`) de regra de negócio (domínio) | n |
| Corpo de erro | JSON com `codigo`, `mensagem` e `campos` (lista para erros de validação) | Contrato único consumido por todos os escopos | n |
| Destinatário das notificações ao funcionário | Endereço de e-mail da oficina definido em configuração | Não há cadastro de funcionários no MVP | n |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Execução local ⭐ MVP

**User Story**: Como avaliador, quero subir a solução com um comando para testar a API sem configurar nada além do Docker.

**Why P1**: Entregável obrigatório (Dockerfile, docker-compose, README).

**Acceptance Criteria**:

1. WHEN `docker compose up` é executado na raiz THEN o sistema SHALL subir a aplicação e o PostgreSQL e responder HTTP 200 em `/actuator/health` ou no Swagger UI.
2. WHEN a aplicação inicia com banco vazio THEN o sistema SHALL aplicar todas as migrations Flyway de `src/main/resources/db/migration` antes de aceitar requisições.
3. IF o banco não está acessível na inicialização THEN o sistema SHALL encerrar com erro de inicialização em vez de subir sem banco.

**Independent Test**: Subir o compose em máquina limpa e abrir o Swagger.

---

### P1: Autenticação JWT do funcionário ⭐ MVP

**User Story**: Como funcionário, quero obter um token com minhas credenciais para usar as APIs administrativas.

**Why P1**: Requisito obrigatório de segurança.

**Acceptance Criteria**:

1. WHEN o funcionário envia usuário e senha válidos ao endpoint de autenticação THEN o sistema SHALL responder 200 com um JWT assinado com validade de 60 minutos.
2. IF usuário ou senha são inválidos THEN o sistema SHALL responder 401 sem emitir token.
3. IF uma requisição à API administrativa chega sem token, com token expirado ou com assinatura inválida THEN o sistema SHALL responder 401.
4. WHEN uma requisição chega em `/cliente/**`, no endpoint de autenticação ou na documentação OpenAPI THEN o sistema SHALL processá-la sem exigir token (AD-001).

**Independent Test**: Chamar um endpoint administrativo sem token (401), autenticar e repetir com o token (200).

---

### P1: Erros padronizados ⭐ MVP

**User Story**: Como consumidor da API, quero um formato único de erro para tratar falhas sem casos especiais.

**Why P1**: Todos os escopos dependem do mesmo contrato.

**Acceptance Criteria**:

1. IF o request viola uma validação `@Valid` THEN o sistema SHALL responder 400 com `codigo`, `mensagem` e a lista `campos` contendo cada campo inválido.
2. IF o caso de uso lança `DomainException` THEN o sistema SHALL responder 422 com `codigo` e `mensagem` da exceção.
3. IF o recurso pedido não existe THEN o sistema SHALL responder 404 com `codigo` e `mensagem`.
4. IF ocorre um erro não tratado THEN o sistema SHALL responder 500 com mensagem genérica, sem stack trace no corpo.

**Independent Test**: Teste de integração que provoca cada tipo de erro e confere status e corpo.

---

### P1: Guardrails de arquitetura e cobertura ⭐ MVP

**User Story**: Como time, quero que a build falhe quando a arquitetura ou a cobertura regridem.

**Why P1**: `AGENTS.md` define as regras; sem verificação automática elas se perdem.

**Acceptance Criteria**:

1. IF uma classe de `domain` ou `application` depende de pacote `org.springframework` ou `jakarta.persistence` THEN o sistema SHALL falhar no teste ArchUnit.
2. IF uma classe de `domain` depende de `application` ou `infrastructure`, ou uma de `application` depende de `infrastructure` THEN o sistema SHALL falhar no teste ArchUnit.
3. IF uma classe do contexto X em `domain` ou `application` depende de classe de outro contexto em `domain` ou `application` THEN o sistema SHALL falhar no teste ArchUnit.
4. IF a cobertura de linhas de algum pacote de `domain` ou `application` fica abaixo de 80% THEN o sistema SHALL falhar `./mvnw verify`.

**Independent Test**: Introduzir uma dependência proibida em branch descartável e ver `./mvnw test` falhar.

---

### P2: Documentação OpenAPI

**User Story**: Como avaliador, quero navegar pela API no Swagger.

**Why P2**: Entregável obrigatório, mas cresce com cada escopo.

**Acceptance Criteria**:

1. WHEN o Swagger UI é aberto THEN o sistema SHALL listar todos os endpoints com o esquema de autenticação Bearer declarado para os administrativos.

**Independent Test**: Abrir o Swagger UI e autenticar por ele.

---

### P2: Notificações simuladas

**User Story**: Como avaliador, quero ver cada e-mail que o sistema enviaria, com payload e resposta simulada, para acompanhar o processo.

**Why P2**: Usado a partir do escopo de abertura da OS; Decisões do MVP exigem payload e resposta expostos pela API.

**Acceptance Criteria**:

1. WHEN um caso de uso envia uma notificação pela porta de e-mail THEN o sistema SHALL persistir destinatário, assunto, corpo, data/hora, referência da OS (quando houver) e a resposta simulada do provedor.
2. WHEN o funcionário consulta as notificações de uma OS THEN o sistema SHALL retorná-las em ordem cronológica.
3. The sistema SHALL devolver resposta simulada de sucesso com identificador único para cada envio.

**Independent Test**: Disparar uma notificação por um caso de uso de teste e consultá-la pela API.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| FUND-01 | P1: Execução local | - | Pending |
| FUND-02 | P1: Autenticação JWT | - | Pending |
| FUND-03 | P1: Erros padronizados | - | Pending |
| FUND-04 | P1: Guardrails | - | Pending |
| FUND-05 | P2: OpenAPI | - | Pending |
| FUND-06 | P2: Notificações simuladas | - | Pending |

**Coverage:** 6 total, 0 mapped tasks, 6 unmapped ⚠️

---

## Success Criteria

- [ ] Máquina limpa com Docker sobe a solução e autentica em menos de 5 minutos seguindo o README.
- [ ] `./mvnw verify` verde com ArchUnit e JaCoCo ativos.
