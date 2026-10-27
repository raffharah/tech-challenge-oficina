# Abertura da OS e Orçamento Tasks

## Execution Protocol (MANDATORY -- do not skip)

Implement these tasks with the `tlc-spec-driven` skill: **activate it by name and follow its Execute flow and Critical Rules.** Do not search for skill files by filesystem path. The skill is the source of truth for the full flow (per-task cycle, sub-agent delegation, adequacy review, Verifier, discrimination sensor).

**If the skill cannot be activated, STOP and tell the user - do not proceed without it.**

---

**Design**: `.specs/features/abertura-os-orcamento/design.md`
**Status**: Reviewed

---

## Test Coverage Matrix

> Same matrix as `fundacao-plataforma/tasks.md` - confirm before Execute.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Domain | unit | Todos os ramos; 1:1 com ACs; todas as combinações de status | `src/test/java/br/com/fiap/oficina/domain/ordemservico/*Test.java` | `./mvnw test` |
| Application | unit | 1:1 com ACs e erros; repositório e portas em memória | `src/test/java/br/com/fiap/oficina/application/ordemservico/*Test.java` | `./mvnw test` |
| Controller / RepositoryImpl / Gateway | integration | Cada rota: sucesso + bordas + erros | `src/test/java/br/com/fiap/oficina/infrastructure/**/*IT.java` | `./mvnw verify` |
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
T1 → T3 → T4 → T5
T2 → T5
```

### Phase 2: Casos de uso

```
T6 → T7 → T8 → T9 → T10 → T11
```

### Phase 3: Infraestrutura

```
T12 → T13 → T16 → T17 → T18
T14 → T16
T15 → T17
```

---

## Task Breakdown

### T1: Criar StatusOrdemServico com transições

**What**: Enum com os sete status e `podeIrPara(destino)` permitindo só as transições de OS-04.1.
**Where**: `src/main/java/br/com/fiap/oficina/domain/ordemservico/StatusOrdemServico.java`
**Depends on**: None
**Reuses**: -
**Requirement**: OS-04

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `StatusOrdemServicoTest` parametrizado com as 49 combinações: as 6 permitidas retornam `true`, as demais `false`
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T2: Criar Orcamento

**What**: `Orcamento.calcular(servicos, itens)` com total = Σ valor + Σ (quantidade × preço), escala 2, `HALF_EVEN`; records `ServicoDaOrdem` e `ItemDaOrdem`.
**Where**: `src/main/java/br/com/fiap/oficina/domain/ordemservico/Orcamento.java`
**Depends on**: None
**Reuses**: -
**Requirement**: OS-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `OrcamentoTest`: R$ 100,00 + 2 × R$ 35,50 = R$ 171,00 (OS-03.1); arredondamento HALF_EVEN em valor com 3 casas (OS-03.3); sem itens soma só serviços
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T3: Criar OrdemServico com abertura e histórico

**What**: `OrdemServico.abrir(VeiculoIdentificado, Instant)` cria RECEBIDA e muda para EM_DIAGNOSTICO; `mudarPara` valida `podeIrPara` e registra `MudancaDeStatus`; record `VeiculoIdentificado`.
**Where**: `src/main/java/br/com/fiap/oficina/domain/ordemservico/OrdemServico.java`
**Depends on**: T1
**Reuses**: `StatusOrdemServico` (T1)
**Requirement**: OS-01, OS-04

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `OrdemServicoTest`: abertura termina EM_DIAGNOSTICO com histórico [RECEBIDA, EM_DIAGNOSTICO] e data/hora (OS-01.1, OS-04.3); transição inválida lança `DomainException` sem alterar status (OS-04.2)
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T4: Adicionar componentes à OrdemServico

**What**: `incluirServico`, `removerServico`, `incluirItem` (soma quantidade do mesmo item), `removerItem`, todos só EM_DIAGNOSTICO; serviço repetido rejeitado; record `ItemCotado`.
**Where**: `src/main/java/br/com/fiap/oficina/domain/ordemservico/OrdemServico.java` (modify)
**Depends on**: T3
**Reuses**: `Servico` (catálogo), `ServicoDaOrdem`/`ItemDaOrdem` (T2)
**Requirement**: OS-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `OrdemServicoTest`: serviço incluído com valor padrão (OS-02.1); item com preço cotado (OS-02.2); mesmo item soma quantidade; quantidade ≤ 0 rejeitada; remoção (OS-02.3); serviço repetido → `DomainException` (OS-02.6); qualquer comando fora de EM_DIAGNOSTICO → `DomainException` (OS-02.5)
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T5: Adicionar conclusão do diagnóstico à OrdemServico

**What**: `concluirDiagnostico(Instant)` exige ao menos um serviço, calcula `Orcamento` e muda para AGUARDANDO_APROVACAO.
**Where**: `src/main/java/br/com/fiap/oficina/domain/ordemservico/OrdemServico.java` (modify)
**Depends on**: T4, T2
**Reuses**: `Orcamento` (T2)
**Requirement**: OS-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `OrdemServicoTest`: total calculado e status AGUARDANDO_APROVACAO (OS-03.1); sem serviço → `DomainException` sem mudar status (OS-03.2)
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T6: Criar caso de uso AbrirOrdemServico

**What**: Identifica veículo pela porta `CadastroDeVeiculos` e salva a OS aberta; interfaces `CadastroDeVeiculos` e `OrdemServicoRepository`; DTOs `AbrirOrdemServicoDTO` e `OrdemServicoDTO`.
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/AbrirOrdemServico.java`
**Depends on**: None
**Reuses**: `OrdemServico.abrir` (T3), `Clock`
**Requirement**: OS-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `AbrirOrdemServicoTest` com porta e repositório em memória: OS salva EM_DIAGNOSTICO (OS-01.1); porta lançando 422/404 propaga sem salvar (OS-01.2, OS-01.3, OS-01.4)
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T7: Criar caso de uso IncluirServicoNaOrdem

**What**: Busca serviço no `ServicoRepository` (inexistente → 422) e inclui na OS (inexistente → 404).
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/IncluirServicoNaOrdem.java`
**Depends on**: T6
**Reuses**: `ServicoRepositoryEmMemoria` (catálogo), `OrdemServicoRepositoryEmMemoria` (T6)
**Requirement**: OS-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `IncluirServicoNaOrdemTest`: inclusão (OS-02.1), serviço inexistente → 422 (OS-02.4), OS inexistente → 404, repetido → 422 (OS-02.6)
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T8: Criar caso de uso RemoverServicoDaOrdem

**What**: Remove serviço da OS por `servicoId`.
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/RemoverServicoDaOrdem.java`
**Depends on**: T7
**Reuses**: `OrdemServicoRepositoryEmMemoria`
**Requirement**: OS-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `RemoverServicoDaOrdemTest`: removido (OS-02.3); OS fora de diagnóstico → 422 (OS-02.5)
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T9: Criar caso de uso IncluirItemNaOrdem

**What**: Cota o item pela porta `EstoqueDeItens.cotar` e inclui na OS com a quantidade; interface `EstoqueDeItens`.
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/IncluirItemNaOrdem.java`
**Depends on**: T8
**Reuses**: `OrdemServicoRepositoryEmMemoria`
**Requirement**: OS-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `IncluirItemNaOrdemTest` com `EstoqueDeItensEmMemoria`: inclusão com preço cotado (OS-02.2); item inexistente → 422 (OS-02.4)
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T10: Criar caso de uso RemoverItemDaOrdem

**What**: Remove item da OS por `itemId`.
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/RemoverItemDaOrdem.java`
**Depends on**: T9
**Reuses**: `OrdemServicoRepositoryEmMemoria`
**Requirement**: OS-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `RemoverItemDaOrdemTest`: removido (OS-02.3); fora de diagnóstico → 422 (OS-02.5)
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T11: Criar caso de uso ConcluirDiagnostico

**What**: Conclui o diagnóstico da OS e devolve o total do orçamento.
**Where**: `src/main/java/br/com/fiap/oficina/application/ordemservico/ConcluirDiagnostico.java`
**Depends on**: T10
**Reuses**: `OrdemServico.concluirDiagnostico` (T5)
**Requirement**: OS-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `ConcluirDiagnosticoTest`: total e AGUARDANDO_APROVACAO (OS-03.1); sem serviço → 422 (OS-03.2); OS inexistente → 404
- [ ] Gate check passes: `./mvnw test`

**Tests**: unit
**Gate**: quick

---

### T12: Criar migration de ordem de serviço

**What**: Tabelas `ordem_servico`, `ordem_servico_servico`, `ordem_servico_item`, `ordem_servico_historico` do design, com FKs para `veiculo`, `servico`, `item_estoque`.
**Where**: `src/main/resources/db/migration/V4__ordem_servico.sql`
**Depends on**: None
**Reuses**: modelo de dados do design
**Requirement**: OS-01, OS-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] Flyway aplica em `OficinaApplicationIT`
- [ ] Gate check passes: `./mvnw verify`

**Tests**: none
**Gate**: build

---

### T13: Criar persistência de OrdemServico

**What**: `OrdemServicoEntity` com coleções (`@ElementCollection` para componentes e histórico), `OrdemServicoJpaRepository` e `OrdemServicoRepositoryImpl`.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/db/ordemservico/OrdemServicoRepositoryImpl.java`
**Depends on**: T12
**Reuses**: padrão de persistência do design
**Requirement**: OS-01, OS-02, OS-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `OrdemServicoRepositoryImplIT`: salvar e recarregar OS com serviços, itens, total e histórico em ordem
- [ ] Remover veículo, serviço ou item referenciado pela OS → `DataIntegrityViolationException` (CLI-03.7, SRV-01.6, EST-01.6)
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

---

### T14: Implementar porta CadastroDeVeiculos

**What**: `CadastroDeVeiculosImpl` usa `ConsultarClientes` e `ConsultarVeiculos`; veículo de outro cliente → `DomainException` (422).
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/gateway/cliente/CadastroDeVeiculosImpl.java`
**Depends on**: None
**Reuses**: casos de uso de `application/cliente`
**Requirement**: OS-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `CadastroDeVeiculosImplIT`: identifica com CPF mascarado e placa minúscula; CPF inválido → 422; cliente/veículo inexistente → 404; veículo de outro cliente → 422
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

---

### T15: Implementar EstoqueDeItens.cotar

**What**: `EstoqueDeItensImpl.cotar` usa `ConsultarItens`; item inexistente vira `DomainException` (422).
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/gateway/estoque/EstoqueDeItensImpl.java`
**Depends on**: None
**Reuses**: `ConsultarItens` (estoque)
**Requirement**: OS-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `EstoqueDeItensImplIT`: cotação retorna sku, nome e preço de venda; inexistente → `DomainException`
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

---

### T16: Criar endpoint de abertura da OS

**What**: `OrdemServicoController` com `POST /ordens-servico`; registra `AbrirOrdemServico` e as portas em `BeanConfig`.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/http/ordemservico/OrdemServicoController.java`
**Depends on**: T13, T14
**Reuses**: `ErroHandler`, `SecurityConfig`
**Requirement**: OS-01

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `OrdemServicoAberturaIT` (com JWT): 201 EM_DIAGNOSTICO; CPF/placa inválidos 422; inexistentes 404; veículo de outro cliente 422; sem token 401
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

---

### T17: Criar endpoints de componentes da OS

**What**: `POST /ordens-servico/{id}/servicos`, `DELETE .../servicos/{servicoId}`, `POST .../itens`, `DELETE .../itens/{itemId}`; registra os casos de uso em `BeanConfig`.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/http/ordemservico/OrdemServicoController.java` (modify)
**Depends on**: T16, T15
**Reuses**: padrão de T16
**Requirement**: OS-02

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `OrdemServicoComponentesIT`: inclusão e remoção de serviço e item; inexistentes 422; repetido 422; quantidade 0 → 400/422; fora de diagnóstico 422
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full

---

### T18: Criar endpoint de orçamento

**What**: `POST /ordens-servico/{id}/orcamento` conclui o diagnóstico; registra `ConcluirDiagnostico` em `BeanConfig`.
**Where**: `src/main/java/br/com/fiap/oficina/infrastructure/http/ordemservico/OrdemServicoController.java` (modify)
**Depends on**: T17
**Reuses**: padrão de T16
**Requirement**: OS-03

**Tools**:
- MCP: NONE
- Skill: NONE

**Done when**:
- [ ] `OrdemServicoOrcamentoIT`: total R$ 171,00 e AGUARDANDO_APROVACAO; sem serviço 422; segunda conclusão 422
- [ ] Gate check passes: `./mvnw verify`

**Tests**: integration
**Gate**: full
