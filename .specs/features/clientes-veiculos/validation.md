# Clientes e Veículos Validation

**Date**: 2026-10-27
**Spec**: `.specs/features/clientes-veiculos/spec.md`
**Diff range**: `main..HEAD` + working tree (nenhum commit, a pedido do usuário)
**Verifier**: inline (sem sub-agent, a pedido do usuário); mesma sessão do autor, então o resultado vale como verificação por evidência, não como revisão independente

---

## Task Completion

| Task | Status | Notes |
| ---- | ------ | ----- |
| T1–T17 | ✅ Done | Todas marcadas `[x]` em `tasks.md` |

---

## Spec-Anchored Acceptance Criteria

### P1: CRUD de clientes (CLI-01)

| Criterion | Spec-defined outcome | `file:line` + assertion | Result |
| --- | --- | --- | --- |
| AC1 CPF / CNPJ numérico / CNPJ alfanumérico válido | 201 | `ClienteControllerIT.java:52` `cadastraCpfCnpjNumericoEAlfanumerico`, status 201 para os 3 formatos; `CpfCnpjTest.java:24,29` | ✅ PASS |
| AC2 formato, DV errado ou dígitos iguais | 422, sem persistir | `ClienteControllerIT.java:70` `cpfCnpjInvalidoRetorna422SemPersistir`; `CpfCnpjTest.java:35,41,47`; `CadastrarClienteTest.java:33` | ✅ PASS |
| AC3 CPF/CNPJ normalizado repetido | 409 | `ClienteControllerIT.java:87`; `CadastrarClienteTest.java:40` (máscara diferente) | ✅ PASS |
| AC4 atualiza nome mantendo CPF/CNPJ | nome novo, CPF/CNPJ igual | `ClienteControllerIT.java:96`; `AtualizarClienteTest.java:15` | ✅ PASS |
| AC5 lista paginada | página com `itens/pagina/tamanho/total` | `ClienteControllerIT.java:116`; `ConsultarClientesTest.java:56`; `ClienteRepositoryImplIT.java:61` | ✅ PASS |
| AC6 remove cliente com veículos | 409, cliente mantido | `ClienteControllerIT.java:165`; `RemoverClienteTest.java:29` | ✅ PASS |

### P1: Identificação do cliente (CLI-02)

| Criterion | Spec-defined outcome | `file:line` + assertion | Result |
| --- | --- | --- | --- |
| AC1 busca numérica/alfanumérica, com ou sem máscara | cliente retornado | `ClienteControllerIT.java:131`; `ConsultarClientesTest.java:18` | ✅ PASS |
| AC2 não cadastrado | 404 | `ClienteControllerIT.java:144`; `ConsultarClientesTest.java:33` | ✅ PASS |
| AC3 formato inválido | 422 | `ClienteControllerIT.java:144`; `ConsultarClientesTest.java:39` | ✅ PASS |
| AC4 minúsculas em CNPJ alfanumérico | normaliza para maiúsculas | `ConsultarClientesTest.java:26`; `CpfCnpjTest.java:29` | ✅ PASS |

### P1: CRUD de veículos (CLI-03)

| Criterion | Spec-defined outcome | `file:line` + assertion | Result |
| --- | --- | --- | --- |
| AC1 placa antiga/Mercosul | 201, placa normalizada | `VeiculoControllerIT.java:64,79`; `CadastrarVeiculoTest.java:28,38`; `PlacaTest.java:14,19` | ✅ PASS |
| AC2 placa fora dos padrões | 422 | `VeiculoControllerIT.java:87`; `PlacaTest.java:25` | ✅ PASS |
| AC3 placa normalizada repetida | 409 | `VeiculoControllerIT.java:95`; `CadastrarVeiculoTest.java:49` | ✅ PASS |
| AC4 ano fora de 1900..ano corrente+1 | 422 | `VeiculoControllerIT.java:104`; `VeiculoTest.java:19,25`; `CadastrarVeiculoTest.java:57` | ✅ PASS |
| AC5 atualiza marca/modelo/ano | placa e cliente mantidos | `VeiculoControllerIT.java:134`; `VeiculoTest.java:39`; `AtualizarVeiculoTest.java:20` | ✅ PASS |
| AC6 lista veículos do cliente | só os dele | `VeiculoControllerIT.java:155`; `ConsultarVeiculosTest.java:40`; `VeiculoRepositoryImplIT.java:60` | ✅ PASS |
| AC7 remove veículo referenciado por OS | 409 | Sem teste: a tabela de OS não existe nesta fase. `ErroHandler.java:41` mapeia `DataIntegrityViolationException` para 409 e cobrirá a FK quando a OS existir | ⚠️ Adiado (OS fora do escopo da fase) |

**Status**: ✅ 16/17 ACs cobertos; AC7 de veículos adiado até existir `ordemservico`.

**Regra do CNPJ alfanumérico**: posições 1–12 alfanuméricas e 13–14 numéricas, conforme IN RFB 2.119/2022 (alterada pela 2.229/2024) e Nota Técnica Conjunta 2025.001. `CpfCnpj.java:10` implementa `[0-9A-Z]{12}\d{2}`. Sem spec-precision gap.

---

## Discrimination Sensor

Cópia isolada fora do repositório; árvore real intacta (`git status --porcelain` igual ao baseline).

| # | File:line | Mutação | Killed? |
| - | --------- | ------- | ------- |
| 1 | `domain/cliente/Veiculo.java:27` | limite do ano `+1` → `+2` | ✅ Killed |
| 2 | `domain/cliente/CpfCnpj.java:11` | último peso do DV2 `2` → `3` | ✅ Killed |
| 3 | `domain/cliente/CpfCnpj.java:55` | `distinct().count() == 1` → `== 2` (dígitos iguais) | ✅ Killed |
| 4 | `application/cliente/RemoverCliente.java:22` | remove checagem de veículos | ✅ Killed |
| 5 | `application/cliente/CadastrarVeiculo.java` | condições invertidas | ✅ Killed |
| 6 | `domain/cliente/Cliente.java:22` | nome em branco aceito | ✅ Killed |

Mutação descartada: remover o guarda numérico em `CpfCnpj.java:39` é equivalente (um CNPJ com 14 caracteres iguais precisa ser todo numérico), logo não muda comportamento.

**Sensor depth**: lightweight
**Result**: 6/6 killed — PASS ✅

---

## Code Quality

| Check | Pass? |
| ----- | ----- |
| Sem features além do pedido | ✅ |
| Sem abstração de uso único | ✅ (`Pagina` é usada por listagem de clientes) |
| Só arquivos necessários | ✅ |
| Segue padrões do projeto (AGENTS.md: camadas, `executar`, DTOs, `BeanConfig`) | ✅ |
| Testes mapeiam ACs | ✅ |
| Domínio 1:1 com ACs; rotas com caminho feliz, borda e erro | ✅ |
| Diretrizes documentadas seguidas | AGENTS.md (TDD, F.I.R.S.T., JaCoCo) |

---

## Edge Cases

- [x] CPF/CNPJ com máscara e minúsculas normalizados
- [x] CNPJ numérico com 14 dígitos iguais rejeitado
- [x] Request sem nome/marca → 400 (`ClienteControllerIT.java:80`, `VeiculoControllerIT.java:127`)
- [x] Sem token → 401 (`ClienteControllerIT.java:182`, `VeiculoControllerIT.java:184`)
- [x] Cliente inexistente ao cadastrar veículo → 404 (`VeiculoControllerIT.java:120`)

---

## Gate Check

- **Gate command**: `./mvnw verify`
- **Result**: 122 passed, 0 failed, 0 skipped (surefire + failsafe; JaCoCo 80% sem falha)
- **Test count before feature**: 0 em `cliente`
- **Skipped tests**: nenhum

---

## Requirement Traceability Update

| Requirement | Previous Status | New Status |
| ----------- | --------------- | ---------- |
| CLI-01 | Implemented | ✅ Verified |
| CLI-02 | Implemented | ✅ Verified |
| CLI-03 | Implemented | ✅ Verified (AC7 adiado) |

---

## Summary

**Overall**: ✅ Ready
**Spec-anchored check**: 16/17 ACs com evidência; AC7 de veículos adiado (OS inexistente); 0 spec-precision gaps
**Sensor**: 6/6 mutações mortas
**Gate**: 122 passed
**Next steps**: cobrir AC7 de veículos quando `ordemservico` existir; commit a pedido do usuário.
