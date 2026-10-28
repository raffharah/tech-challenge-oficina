# Abertura da OS e Orçamento Validation

**Date**: 2026-10-28
**Spec**: `.specs/features/abertura-os-orcamento/spec.md`
**Diff range**: working tree sobre `1548184` (sem commits, por instrução do usuário)
**Verifier**: mesma sessão, sem subagent (instrução do usuário); author = verifier, evidência por `file:line`

---

## Task Completion

| Task | Status | Notes |
| ---- | ------ | ----- |
| T1-T5 | ✅ Done | Domínio |
| T6-T11 | ✅ Done | Casos de uso |
| T12-T18 | ✅ Done | Migration, persistência, gateways, endpoints |

---

## Spec-Anchored Acceptance Criteria

| Criterion | Spec-defined outcome | `file:line` + assertion | Result |
| --- | --- | --- | --- |
| OS-01.1 abrir | 201, EM_DIAGNOSTICO, histórico RECEBIDA→EM_DIAGNOSTICO | `src/test/java/br/com/fiap/oficina/infrastructure/http/ordemservico/OrdemServicoAberturaIT.java:47` - `status().isCreated()`; `:49` - `jsonPath("$.status").value("EM_DIAGNOSTICO")`; `src/test/java/br/com/fiap/oficina/domain/ordemservico/OrdemServicoTest.java:28` - `assertThat(ordem.getStatus()).isEqualTo(EM_DIAGNOSTICO)` | ✅ PASS |
| OS-01.2 CPF/placa inválidos | 422 sem criar | `OrdemServicoAberturaIT.java:61` - `status().isUnprocessableEntity()`; `src/test/java/br/com/fiap/oficina/application/ordemservico/AbrirOrdemServicoTest.java:47` - `assertThat(ordens.quantidade()).isZero()` | ✅ PASS |
| OS-01.3 inexistente | 404 | `OrdemServicoAberturaIT.java:75` - `status().isNotFound()` | ✅ PASS |
| OS-01.4 outro cliente | 422 | `OrdemServicoAberturaIT.java:85` - `status().isUnprocessableEntity()`; `:86` - `value("VEICULO_DE_OUTRO_CLIENTE")` | ✅ PASS |
| OS-02.1 serviço com valor padrão | valor 100.00 | `src/test/java/br/com/fiap/oficina/infrastructure/http/ordemservico/OrdemServicoComponentesIT.java:65` - `status().isCreated()`; `src/test/java/br/com/fiap/oficina/domain/ordemservico/OrdemServicoTest.java:55` - `assertThat(ordem.getServicos()).containsExactly(ServicoDaOrdem(1L,"Troca de oleo",100.00))` | ✅ PASS |
| OS-02.2 item com preço cotado | preço 35.50 | `OrdemServicoTest.java:65` - `assertThat(ordem.getItens()).containsExactly(ItemDaOrdem(10L,...,4,35.50))`; `src/test/java/br/com/fiap/oficina/application/ordemservico/IncluirItemNaOrdemTest.java:33` - `assertThat(dto.itens()).containsExactly(...)` | ✅ PASS |
| OS-02.3 remover | retirado | `OrdemServicoTest.java:98` - `assertThat(ordem.getServicos()).isEmpty()`; `OrdemServicoComponentesIT.java:94` - `isZero()` | ✅ PASS |
| OS-02.4 inexistente | 422 | `IncluirServicoNaOrdemTest.java:38` - `isExactlyInstanceOf(DomainException.class)`; `OrdemServicoComponentesIT.java:51` - `status().is(esperado)` com 422 (`:99`) | ✅ PASS |
| OS-02.5 fora de diagnóstico | 422 | `OrdemServicoTest.java:119` - `isInstanceOf(DomainException.class)`; `OrdemServicoComponentesIT.java:135` - `isUnprocessableEntity()` | ✅ PASS |
| OS-02.6 serviço repetido | 422 sem duplicar | `OrdemServicoTest.java:107,109` - `hasSize(1)`; `OrdemServicoComponentesIT.java:116` - `isEqualTo(1)` | ✅ PASS |
| OS-03.1 total + status | 171.00, AGUARDANDO_APROVACAO | `OrdemServicoTest.java:136` - `isEqualTo(new BigDecimal("171.00"))`; `:137`; `OrcamentoTest.java:16`; `OrdemServicoOrcamentoIT.java:54` - `status().isOk()` | ✅ PASS |
| OS-03.2 sem serviço | 422 sem calcular | `OrdemServicoTest.java:147,149,150` - `getTotal()).isNull()`; `OrdemServicoOrcamentoIT.java:63` - `isUnprocessableEntity()` | ✅ PASS |
| OS-03.3 HALF_EVEN, 2 casas | 10.005→10.00; 10.015→10.02 | `OrcamentoTest.java:24,25` - `isEqualTo(new BigDecimal("10.00"))` / `("10.02")` | ✅ PASS |
| OS-04.1 lista de transições | 6 permitidas, 49 combinações | `StatusOrdemServicoTest.java:40` - `assertThat(origem.podeIrPara(destino)).isEqualTo(esperado)`; `:45,46` | ✅ PASS |
| OS-04.2 inválida | DomainException sem alterar status | `OrdemServicoTest.java:43,45` - `isEqualTo(AGUARDANDO_APROVACAO)` | ✅ PASS |
| OS-04.3 histórico | status + data/hora | `OrdemServicoTest.java:32,138` - `MudancaDeStatus(AGUARDANDO_APROVACAO, CONCLUSAO)` | ✅ PASS |

**Status**: ✅ All ACs covered. Nenhum spec-precision gap.

---

## Discrimination Sensor

Scratch: cópia da árvore em diretório temporário (sem `git stash`); baseline unit e IT verdes na cópia.

| Mutation | File | Description | Killed? |
| -------- | ---- | ----------- | ------- |
| 1 | `domain/ordemservico/Orcamento.java` | `HALF_EVEN` → `HALF_UP` | ✅ Killed |
| 2 | `domain/ordemservico/OrdemServico.java` | soma de quantidade → substitui | ✅ Killed |
| 3 | `domain/ordemservico/StatusOrdemServico.java` | AGUARDANDO_APROVACAO também → ENTREGUE | ✅ Killed |
| 4 | `domain/ordemservico/OrdemServico.java` | removida exigência de serviço | ✅ Killed |
| 5 | `application/ordemservico/ConcluirDiagnostico.java` | removida chamada `concluirDiagnostico` | ✅ Killed |
| 6 | `domain/ordemservico/OrdemServico.java` | removido `removerServico` | ✅ Killed |
| 7 | `infrastructure/gateway/cliente/CadastroDeVeiculosImpl.java` | removida checagem de dono | ✅ Killed (IT) |
| 8 | `infrastructure/gateway/estoque/EstoqueDeItensImpl.java` | 404 não vira 422 | ✅ Killed (IT) |

**Sensor depth**: lightweight
**Result**: 8/8 killed - PASS ✅. `git status --porcelain` da árvore real idêntico ao baseline.

---

## Code Quality

| Principle | Status |
| --- | --- |
| Minimum code | ✅ |
| Surgical changes | ✅ (extra: `cascade` nos `truncate` dos ITs existentes, exigido pelas novas FKs) |
| No scope creep | ✅ (`aprovar`/`finalizar`/`baixar` ficaram para escopos 6-8) |
| Matches patterns | ✅ |
| Spec-anchored outcome check | ✅ |
| Per-layer Coverage Expectation | ✅ (rotas: sucesso, borda e erro; 401 em abertura e componentes) |
| Every test maps to a requirement | ✅ |
| Documented guidelines followed | AGENTS.md (F.I.R.S.T., sufixos `*Test`/`*IT`) |

---

## Edge Cases

- [x] Quantidade 0 → 422; ausente → 400 (`OrdemServicoComponentesIT.java:120-124`)
- [x] Segunda conclusão → 422 (`OrdemServicoOrcamentoIT.java:74`)
- [x] FK impede remover veículo/serviço/item referenciado (`OrdemServicoRepositoryImplIT.java`, `removerVeiculoServicoOuItemReferenciadoViolaIntegridade`)

---

## Gate Check

- **Gate command**: `./mvnw verify`
- **Result**: 320 passed (surefire 197, failsafe 123), 0 failed, 0 skipped; JaCoCo ≥ 80% ok
- **Skipped tests**: none
- **Failures**: none

---

## Requirement Traceability Update

| Requirement | Previous Status | New Status |
| --- | --- | --- |
| OS-01..OS-04 | Implemented | ✅ Verified |

---

## Summary

**Overall**: ✅ Ready

**Spec-anchored check**: 16/16 ACs matched spec outcome
**Sensor**: 8/8 mutations killed
**Gate**: 320 passed

**What works**: Abertura → diagnóstico → orçamento ponta a ponta via API.

**Issues found**: none. Observação: verificação feita pelo mesmo agente que implementou (sem subagent), então author ≠ verifier não vale.

**Next steps**: commits por fase.
