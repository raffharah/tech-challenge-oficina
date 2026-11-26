# Notificação de Status por E-mail Tasks

## Execution Protocol (MANDATORY -- do not skip)

Implement these tasks with the `tlc-spec-driven` skill: **activate it by name and follow its Execute flow and Critical Rules.** Do not search for skill files by filesystem path. The skill is the source of truth for the full flow (per-task cycle, sub-agent delegation, adequacy review, Verifier, discrimination sensor).

**If the skill cannot be activated, STOP and tell the user - do not proceed without it.**

---

**Design**: `.specs/features/notificacao-status/design.md`
**Pré-requisito**: `abertura-os-completa` concluída (`ClienteDTO.email`)
**Status**: Fase 1 concluída (T1-T7)

---

## Test Coverage Matrix

Guidelines found: `AGENTS.md`, `pom.xml`.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Application | unit | Cada transição notifica a OS salva; caminho de erro não notifica | `src/test/java/br/com/fiap/oficina/application/ordemservico/*Test.java` | `./mvnw test` |
| Gateway | integration | Envio depois do commit, conteúdo, ausência de envio no rollback, falha de SMTP sem efeito no status | `src/test/java/br/com/fiap/oficina/infrastructure/gateway/**/*IT.java` | `./mvnw verify` |
| Config / Compose / Docs | none | - | - | build gate only |

## Gate Check Commands

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | Tasks de domain/application | `./mvnw test` |
| Full | Tasks com `*IT` | `./mvnw verify` |
| Build | Fim de fase ou task sem teste | `./mvnw verify` |

---

## Execution Plan

### Phase 1: Porta e casos de uso

```
T1 → T2 → T3 → T4 → T5 → T6 → T7
```

### Phase 2: E-mail e ambiente local

```
T8 → T9 → T10
```

---

## Task Breakdown

### T1: Notificar a abertura da OS

**What**: Criar a porta `NotificacaoDeStatus.notificar(OrdemServico)` e o duplo de teste `NotificacaoDeStatusEmMemoria`. `AbrirOrdemServico` recebe a porta e notifica a OS salva. `BeanConfig` usa um bean provisório que não envia nada, substituído em T8.
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/AbrirOrdemServico.java` (modify)
**Depends on**: None
**Reuses**: `AbrirOrdemServicoTest`
**Requirement**: NTF-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `AbrirOrdemServicoTest`: notifica a OS `RECEBIDA` com `id` (NTF-01.1); abertura com serviço inexistente não notifica (NTF-01.4)
- [x] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

**Commit**: `feat(ordemservico): notifica abertura da OS`

---

### T2: Notificar o início do diagnóstico

**What**: `IniciarDiagnostico` recebe a porta e notifica a OS salva.
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/IniciarDiagnostico.java` (modify)
**Depends on**: T1
**Reuses**: `NotificacaoDeStatusEmMemoria`
**Requirement**: NTF-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `IniciarDiagnosticoTest`: notifica `EM_DIAGNOSTICO`; transição inválida não notifica
- [x] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

**Commit**: `feat(ordemservico): notifica inicio do diagnostico`

---

### T3: Notificar o orçamento gerado

**What**: `ConcluirDiagnostico` recebe a porta e notifica a OS salva, com o total.
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/ConcluirDiagnostico.java` (modify)
**Depends on**: T2
**Reuses**: `NotificacaoDeStatusEmMemoria`
**Requirement**: NTF-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `ConcluirDiagnosticoTest`: notifica `AGUARDANDO_APROVACAO` com `total` preenchido (NTF-01.3); `ORDEM_SEM_SERVICO` não notifica
- [x] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

**Commit**: `feat(ordemservico): notifica orcamento gerado`

---

### T4: Notificar a aprovação do orçamento

**What**: `AprovarOrcamento` recebe a porta e notifica a OS salva.
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/AprovarOrcamento.java` (modify)
**Depends on**: T3
**Reuses**: `NotificacaoDeStatusEmMemoria`, `EstoqueDeItensEmMemoria`
**Requirement**: NTF-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `AprovarOrcamentoTest`: notifica `EM_EXECUCAO`; `SALDO_INSUFICIENTE` não notifica (NTF-01.4)
- [x] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

**Commit**: `feat(ordemservico): notifica aprovacao do orcamento`

---

### T5: Notificar a recusa do orçamento

**What**: `RejeitarOrcamento` recebe a porta e notifica a OS salva.
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/RejeitarOrcamento.java` (modify)
**Depends on**: T4
**Reuses**: `NotificacaoDeStatusEmMemoria`
**Requirement**: NTF-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `RejeitarOrcamentoTest`: notifica `CANCELADA`; transição inválida não notifica
- [x] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

**Commit**: `feat(ordemservico): notifica recusa do orcamento`

---

### T6: Notificar a finalização da OS

**What**: `FinalizarOrdemServico` recebe a porta e notifica a OS salva.
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/FinalizarOrdemServico.java` (modify)
**Depends on**: T5
**Reuses**: `NotificacaoDeStatusEmMemoria`
**Requirement**: NTF-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `FinalizarOrdemServicoTest`: notifica `FINALIZADA`; transição inválida não notifica
- [x] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

**Commit**: `feat(ordemservico): notifica finalizacao da OS`

---

### T7: Notificar a entrega do veículo

**What**: `EntregarVeiculo` recebe a porta e notifica a OS salva.
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/EntregarVeiculo.java` (modify)
**Depends on**: T6
**Reuses**: `NotificacaoDeStatusEmMemoria`
**Requirement**: NTF-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [x] `EntregarVeiculoTest`: notifica `ENTREGUE`; transição inválida não notifica
- [x] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

**Commit**: `feat(ordemservico): notifica entrega do veiculo`

---

### T8: Enviar o e-mail depois do commit

**What**:
- `pom.xml` ganha `spring-boot-starter-mail`.
- Criar `NotificacaoDeStatusImpl`:
  - busca o e-mail por `ConsultarClientes`;
  - monta um `SimpleMailMessage` com OS, placa, status e, em `AGUARDANDO_APROVACAO`, o total;
  - envia em `afterCommit`;
  - registra `MailException` em log com o número da OS.
- `application.yml` ganha `spring.mail.*` e `oficina.notificacao.remetente`.
- `BeanConfig` troca o bean provisório de T1 por esta implementação.

**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/gateway/notificacao/NotificacaoDeStatusImpl.java`
**Depends on**: None
**Reuses**: `ConsultarClientes`, `TestcontainersConfig`
**Requirement**: NTF-01, NTF-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `NotificacaoDeStatusImplIT` (`@MockitoBean JavaMailSender`):
  - OS levada de `RECEBIDA` a `ENTREGUE` pela API gera 6 envios ao e-mail do titular, com o status de cada etapa (NTF-01.1, NTF-01.2);
  - o e-mail de `AGUARDANDO_APROVACAO` traz o total (NTF-01.3);
  - `SALDO_INSUFICIENTE` não gera envio (NTF-01.4);
  - o envio acontece depois do commit (NTF-01.5);
  - `MailSendException` mantém 200 e o status gravado (NTF-01.6).
- [ ] `ArquiteturaTest` verde: sem Spring em `domain` e `application` (NTF-01.7)
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

**Commit**: `feat(ordemservico): envia e-mail de mudanca de status depois do commit`

---

### T9: Subir o Mailpit no docker-compose

**What**: Serviço `mailpit` (`axllent/mailpit`, portas 1025 e 8025) e `MAIL_HOST: mailpit` no `app`. O README ganha a URL da caixa local.
**Where**: `docker-compose.yml` (modify)
**Depends on**: T8
**Reuses**: estrutura atual do compose
**Requirement**: NTF-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `docker compose config` válido
- [ ] Gate check passes: `./mvnw verify`

**Tests**: none
**Gate**: build

**Commit**: `chore(docker): adiciona mailpit para os e-mails de status`

---

### T10: Atualizar a documentação

**What**:
- `docs/ddd/linguagem-ubiqua.md`: Decisões do MVP, item "Sistemas externos", com o e-mail no escopo para a notificação de status.
- `docs/ddd/c4/`: servidor de e-mail como sistema externo.
- `docs/ddd/event-storming/fluxo-os.md`: a política "notifica o cliente" em cada mudança de status.

**Where**: `docs/ddd/linguagem-ubiqua.md` (modify)
**Depends on**: T9
**Reuses**: AD-012
**Requirement**: NTF-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] Os documentos descrevem a notificação implementada
- [ ] Gate check passes: `./mvnw verify`

**Tests**: none
**Gate**: build

**Commit**: `docs(ddd): registra notificacao de status por e-mail`
