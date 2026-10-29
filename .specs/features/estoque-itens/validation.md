# Validation: estoque-itens

**Result**: PASS
**Verifier**: inline (sem sub-agent, por decisão do usuário); mesma sessão do autor.
**Diff range**: `662429f..a531ceb`.
**Gate**: `./mvnw verify` verde, 95 testes, 0 falhas, JaCoCo ≥ 80%.

## Acceptance Criteria

| AC | Spec-defined outcome | `file:line` + assertion | Result |
| --- | --- | --- | --- |
| EST-01.1 cadastro SKU novo | 201, saldo físico = inicial | `ItemEstoqueControllerIT.java:65` `jsonPath("$.saldoFisico").value(8)`; `ItemEstoqueTest.java:25` `getSaldoFisico()).isEqualTo(10)` | ✅ PASS |
| EST-01.2 SKU repetido | 409 | `ItemEstoqueControllerIT.java:74` `status().isConflict()` | ✅ PASS |
| EST-01.3 preço ≤ 0 / saldo inicial < 0 | 422 | `ItemEstoqueControllerIT.java:82,86` `isUnprocessableEntity()` | ✅ PASS |
| EST-01.4 atualizar sem mexer no saldo | saldo inalterado | `ItemEstoqueControllerIT.java:110,113` `saldoFisico` = 7; `ItemEstoqueTest.java:62` | ✅ PASS |
| EST-01.5 listar/consultar | tipo, preço, saldo | `ItemEstoqueControllerIT.java:140,154` `saldoFisico` | ✅ PASS |
| EST-01.6 remover item referenciado por OS | 409 | sem teste: tabela de OS não existe nesta feature; 409 vem da FK (escopo 5, `tasks.md` T5). Handler de `DataIntegrityViolationException` → 409 já coberto em `ErroHandler`. | ⚠️ Deferido para a feature que cria a FK |
| EST-02.1 entrada soma | 10 + 5 = 15 | `RegistrarEntradaTest.java:24` `isEqualTo(15)`; `ItemEstoqueControllerIT.java:165` | ✅ PASS |
| EST-02.2 quantidade ≤ 0 | 422 | `ItemEstoqueControllerIT.java:175,179`; `ItemEstoqueTest.java:105` saldo preservado | ✅ PASS |
| EST-03.1 baixa com saldo | subtrai cada quantidade | `BaixarItensTest.java` `baixaTodosOsItensComSaldo` | ✅ PASS |
| EST-03.2 falta | nada baixado; `DomainException` com sku, pedida, saldo | `BaixarItensTest.java:45-46` `containsExactly(new FaltaDeSaldoDTO("B", 3, 1))` | ✅ PASS |
| EST-03.3 nunca negativo | saldo ≥ 0 | `ItemEstoqueTest` `baixaAcimaDoSaldoRejeitadaSemAlterar`; migration `check (saldo_fisico >= 0)` | ✅ PASS |
| Success: concorrência | só uma baixa do último item | `ItemEstoqueRepositoryImplIT.java:158` `ObjectOptimisticLockingFailureException`; `:166` saldo zero | ✅ PASS |

## Discrimination sensor

Scratch: cópia do projeto no scratchpad (sem `.git`/`target`). Árvore real intacta (`git status --porcelain` idêntico antes e depois).

| Mutante | Resultado |
| --- | --- |
| `baixar`: `>` → `>=` (rejeita baixa exata) | killed |
| `baixar`: não subtrai | killed |
| `atualizar` zera saldo | killed |
| `registrarEntrada` aceita 0 | killed |
| `CadastrarItem` sem checagem de SKU | killed |
| `BaixarItens` sem verificação de faltas | killed |
| `BaixarItens` faltas com `>=` | killed |
| `@Version` removido da entity | killed |
| `RegistrarEntrada` não persiste | killed |

9/9 killed, 0 sobreviventes.

## Gaps

1. EST-01.6 sem teste de 409 por OS. Cobrir quando a feature `abertura-os-orcamento` criar a FK.
2. Port `EstoqueDeItens` (baixa via `BaixarItens`) sem consumidor ainda; integra na feature de OS.
3. Verifier inline: não é independente do autor (limitação aceita pelo usuário).
