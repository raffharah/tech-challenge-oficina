# Catálogo de Serviços Validation

**Date**: 2026-10-28
**Spec**: `.specs/features/catalogo-servicos/spec.md`
**Diff range**: `main..HEAD` + working tree (nenhum commit, pedido do usuário)
**Verifier**: inline (sem sub-agent, pedido do usuário); mesma sessão do autor, então o resultado vale como verificação por evidência, não como revisão independente

---

## Task Completion

| Task | Status | Notes |
| ---- | ------ | ----- |
| T1–T8 | ✅ Done | Todas marcadas `[x]` em `tasks.md` |

---

## Spec-Anchored Acceptance Criteria

### P1: CRUD de serviços (SRV-01)

| Criterion | Spec-defined outcome | `file:line` + assertion | Result |
| --- | --- | --- | --- |
| AC1 nome e valor padrão > 0 | persiste, 201 | `ServicoControllerIT.java:52` status 201, `$.nome`, `$.valorPadrao`, `$.total` = 1; `CadastrarServicoTest.java:17`; `ServicoRepositoryImplIT.java:35` | ✅ PASS |
| AC2 valor padrão ≤ 0 | 422 | `ServicoControllerIT.java:64` 0 e -10 → 422 `VALOR_PADRAO_INVALIDO`, `$.total` = 0; `ServicoControllerIT.java:124` (alteração); `ServicoTest.java:21,55`; `CadastrarServicoTest.java:27` | ✅ PASS |
| AC3 nome repetido sem diferenciar maiúsculas | 409 | `ServicoControllerIT.java:88` `TROCA DE OLEO` → 409 `SERVICO_DUPLICADO`; `ServicoControllerIT.java:112` (alteração); `CadastrarServicoTest.java:35`; `AtualizarServicoTest.java:40`; `ServicoRepositoryImplIT.java:82` (índice `lower(nome)`) | ✅ PASS |
| AC4 altera nome ou valor padrão | persiste | `ServicoControllerIT.java:98` PUT e GET com nome e valor novos; `AtualizarServicoTest.java:18`; `ServicoRepositoryImplIT.java:47` | ✅ PASS |
| AC4 sem alterar valores já copiados para OS | OS mantém o valor copiado | Sem teste: OS não existe nesta fase. A cópia do valor é responsabilidade da inclusão do serviço na OS (escopo 5) | ⚠️ Adiado (escopo 5) |
| AC5 lista paginada | `itens/pagina/tamanho/total` | `ServicoControllerIT.java:140`; `ConsultarServicosTest.java:30`; `ServicoRepositoryImplIT.java:60` | ✅ PASS |
| AC6 remover serviço referenciado por OS | 409 | Sem teste: tabela de OS não existe nesta fase. `ErroHandler.java:41` mapeia `DataIntegrityViolationException` para 409 e cobrirá a FK quando a OS existir | ⚠️ Adiado (escopo 5) |

**Status**: ✅ 5/7 critérios cobertos; AC4 (parte da OS) e AC6 adiados até existir `ordemservico` com OS, como em `tasks.md` T5.

---

## Discrimination Sensor

Executado em cópia do projeto no scratchpad (rsync sem `.git`/`target`), descartada ao final. `git status --porcelain` do repositório idêntico ao baseline. A cópia sem mutação passa nos mesmos comandos (unit e IT), então cada falha vem da mutação.

| # | Location | Mutation | Killed? |
| - | --------- | ------- | ------- |
| 1 | `domain/ordemservico/Servico.java:31` | `signum() <= 0` → `< 0` (aceita zero) | ✅ Killed |
| 2 | `domain/ordemservico/Servico.java:20` | `alterar` sem validar valor | ✅ Killed |
| 3 | `application/ordemservico/CadastrarServico.java:17` | remove checagem de nome duplicado | ✅ Killed |
| 4 | `application/ordemservico/AtualizarServico.java:21` | conflito também com o próprio serviço | ✅ Killed |
| 5 | `application/ordemservico/AtualizarServico.java:22` | remove checagem de nome de outro serviço | ✅ Killed |
| 6 | `application/ordemservico/RemoverServico.java:15` | remove 404 | ✅ Killed |
| 7 | `application/ordemservico/ConsultarServicos.java:21` | ignora número da página | ✅ Killed |
| 8 | `infrastructure/db/ordemservico/ServicoJpaRepository.java:8` | `findByNomeIgnoreCase` → `findByNome` | ✅ Killed |
| 9 | `db/migration/V2__servico.sql:7` | índice único em `nome` em vez de `lower(nome)` | ✅ Killed |

**Sensor depth**: lightweight
**Result**: 9/9 killed — PASS ✅

---

## Code Quality

| Check | Pass? |
| ----- | ----- |
| Sem features além do pedido | ✅ |
| Sem abstração de uso único | ✅ (`ServicoRequest` serve POST e PUT, campos iguais) |
| Só arquivos necessários | ✅ |
| Segue padrões do projeto (AGENTS.md: camadas, `executar`, DTOs, `BeanConfig`) | ✅ |
| Testes mapeiam ACs | ✅ |
| Diretrizes documentadas seguidas | AGENTS.md (TDD, F.I.R.S.T., JaCoCo) |

`SPEC_DEVIATION` registrado em `tasks.md` T1: `existePorNome` virou `buscarPorNome`, necessário para o 409 da alteração ignorar o próprio serviço.

`ServicoRepositoryEmMemoria` devolve cópias do modelo, como `ServicoRepositoryImpl`; assim o conflito na alteração não muda o serviço armazenado (`AtualizarServicoTest.java:40`).

---

## Edge Cases

- Request sem nome ou sem valor: 400 (`ServicoControllerIT.java:78`).
- Sem token: 401 (`ServicoControllerIT.java:173`).
- Alterar mantendo o próprio nome em outra caixa: permitido (`AtualizarServicoTest.java:31`).
- Busca, alteração e remoção de id inexistente: 404 (`ServicoControllerIT.java:133,155,168`).

---

## Gate

`./mvnw verify`: 86 unitários e 72 de integração, 0 falhas; JaCoCo dentro do limite. BUILD SUCCESS.

**Result**: PASS
