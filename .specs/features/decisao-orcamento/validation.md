# Decisão sobre o Orçamento Validation

**Date**: 2026-10-28
**Spec**: `.specs/features/decisao-orcamento/spec.md`
**Diff range**: working tree sobre `c64dd77` (branch `feature/decisao-orcamento`, antes dos commits)
**Verifier**: mesma sessão, sem subagent (instrução do usuário); author = verifier, evidência por `file:line`

---

## Task Completion

| Task | Status | Notes |
| ---- | ------ | ----- |
| T1 | ✅ Done | `OrdemServico.aprovar` / `rejeitar` |
| T2 | ✅ Done | `AprovarOrcamento`, porta `EstoqueDeItens.baixar` |
| T3 | ✅ Done | `RejeitarOrcamento` |
| T4 | ✅ Done | `EstoqueDeItensImpl.baixar` |
| T5 | ✅ Done | `ClienteOrdemServicoController`, beans em `BeanConfig` |

---

## Spec-Anchored Acceptance Criteria

| Criterion | Spec-defined outcome | `file:line` + assertion | Result |
| --- | --- | --- | --- |
| ORC-01.1 aprovar com saldo | itens baixados, EM_EXECUCAO | `src/test/java/br/com/fiap/oficina/application/ordemservico/AprovarOrcamentoTest.java:35` - `isEqualTo(EM_EXECUCAO)`; `src/test/java/br/com/fiap/oficina/infrastructure/gateway/estoque/EstoqueDeItensImplIT.java:55` - `saldoFisico()).isEqualTo(6)`; `src/test/java/br/com/fiap/oficina/infrastructure/http/ordemservico/DecisaoOrcamentoIT.java:66` - `value("EM_EXECUCAO")` | ✅ PASS |
| ORC-01.2 sem saldo | AGUARDANDO_APROVACAO, nada baixado, 422 com itens faltantes | `AprovarOrcamentoTest.java:52` - `isEqualTo(AGUARDANDO_APROVACAO)`; `DecisaoOrcamentoIT.java:76,77` - `isUnprocessableEntity()`, `value("SALDO_INSUFICIENTE")`; `:78` - `detalhes.length()` = 1; `EstoqueDeItensImplIT.java:65` - `isEqualTo(10)` | ✅ PASS |
| ORC-01.3 fora de AGUARDANDO_APROVACAO | 422 | `src/test/java/br/com/fiap/oficina/domain/ordemservico/OrdemServicoTest.java:191-194` - `isInstanceOf(DomainException.class)`; `AprovarOrcamentoTest.java:62` + `baixas).isEmpty()`; `DecisaoOrcamentoIT.java:101,103` - `isUnprocessableEntity()` | ✅ PASS |
| ORC-02.1 rejeitar | CANCELADA | `RejeitarOrcamentoTest.java:27,29` - `isEqualTo(CANCELADA)`; `DecisaoOrcamentoIT.java:91` - `value("CANCELADA")` | ✅ PASS |
| ORC-02.2 CANCELADA rejeita comandos | 422 | `OrdemServicoTest.java:207` + `:203-206` - `isInstanceOf(DomainException.class)`; `DecisaoOrcamentoIT.java:95` - `isUnprocessableEntity()` | ✅ PASS |
| Inexistente | 404 | `AprovarOrcamentoTest.java:69`, `RejeitarOrcamentoTest.java:41` - `NaoEncontradoException`; `DecisaoOrcamentoIT.java:108,109` - `isNotFound()` | ✅ PASS |
| Canal sem JWT | endpoints `/cliente` sem token | `DecisaoOrcamentoIT.java:64` - `post(...)` sem `jwt()` → `isOk()` | ✅ PASS |

**Status**: ✅ All ACs covered. Nenhum spec-precision gap.

---

## Discrimination Sensor

Scratch: cópia da árvore em diretório temporário (sem `git stash`).

| Mutation | File | Description | Killed? |
| -------- | ---- | ----------- | ------- |
| 1 | `application/ordemservico/AprovarOrcamento.java` | removida chamada `estoque.baixar` | ✅ Killed (`AprovarOrcamentoTest`, 2 falhas) |
| 2 | `application/ordemservico/AprovarOrcamento.java` | `baixar` antes de `aprovar` | ✅ Killed (`foraDeAguardandoAprovacao...SemChamarEstoque`) |
| 3 | `domain/ordemservico/OrdemServico.java` | `rejeitar` → EM_EXECUCAO | ✅ Killed (`RejeitarOrcamentoTest`, `OrdemServicoTest`) |
| 4 | `infrastructure/gateway/estoque/EstoqueDeItensImpl.java` | quantidade baixada fixa em 1 | ✅ Killed (`EstoqueDeItensImplIT`, 2 falhas) |

**Sensor depth**: lightweight
**Result**: 4/4 killed - PASS ✅. `git status --porcelain` da árvore real idêntico ao baseline. Nota: a mutação 3 permaneceu na cópia durante a 4, então as falhas de `DecisaoOrcamentoIT` na mutação 4 são confundidas; a morte da mutação 4 é atribuída a `EstoqueDeItensImplIT`.

---

## Code Quality

| Principle | Status |
| --- | --- |
| Minimum code | ✅ |
| Surgical changes | ✅ (testes existentes trocaram lambda por `EstoqueDeItensEmMemoria`, exigido por `baixar` na porta) |
| No scope creep | ✅ |
| Matches patterns | ✅ |
| Spec-anchored outcome check | ✅ |
| Per-layer Coverage Expectation | ✅ (domínio 1:1 com ACs; rotas: sucesso, borda e erro) |
| Every test maps to a requirement | ✅ |
| Documented guidelines followed | AGENTS.md (F.I.R.S.T., sufixos `*Test`/`*IT`) |

---

## Edge Cases

- [x] Aprovação repetida após entrada de estoque: OS permanece AGUARDANDO_APROVACAO e transação desfaz (`DecisaoOrcamentoIT.java:80-82`)
- [x] Rejeição seguida de inclusão → 422 (`DecisaoOrcamentoIT.java:95`)

---

## Gate Check

- **Gate command**: `./mvnw verify`
- **Result**: 338 passed (surefire 208, failsafe 130), 0 failed, 0 skipped; JaCoCo ≥ 80% ok
- **Skipped tests**: none
- **Failures**: none

---

## Requirement Traceability Update

| Requirement | Previous Status | New Status |
| --- | --- | --- |
| ORC-01, ORC-02 | In Tasks | ✅ Verified |

---

## Summary

**Overall**: ✅ Ready

**Spec-anchored check**: 7/7 critérios com resultado igual ao da spec
**Sensor**: 4/4 mutations killed
**Gate**: 338 passed

**Issues found**: none. Verificação feita pelo mesmo agente que implementou (sem subagent), então author ≠ verifier não vale.

**Next steps**: commits.
