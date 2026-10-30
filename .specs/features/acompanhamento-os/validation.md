## Validation: acompanhamento-os - PASS ✅

**Verifier**: execução inline pelo orquestrador (sem sub-agent, por pedido do usuário).
**Diff range**: 3dc4570..0a80b60 (b9a173e, b39fa64, 0a80b60)
**Gate**: `./mvnw verify` → 228 testes unitários e 147 de integração, 0 falhas, 0 skips, BUILD SUCCESS.

### Cobertura por AC (evidence-or-zero)

Prefixos: `U` = `src/test/java/br/com/fiap/oficina/application/ordemservico`, `D` = `src/test/java/br/com/fiap/oficina/domain/ordemservico`, `H` = `src/test/java/br/com/fiap/oficina/infrastructure/http/ordemservico`.

| AC | Resultado spec | `file:line` + assertion | Resultado |
| --- | --- | --- | --- |
| ACO-01.1 use case | status, histórico, componentes e total ao titular | `U/ConsultarAndamentoTest.java:35` - `dto.status()).isEqualTo(AGUARDANDO_APROVACAO)`; `:36` histórico `containsExactly(RECEBIDA, EM_DIAGNOSTICO, AGUARDANDO_APROVACAO)` | ✅ |
| ACO-01.1 API | 200 com CPF mascarado | `H/ConsultaClienteIT.java:83-86` - status, `historico.length()` 3, `servicos[0].nome`, `total` 100.00 | ✅ |
| ACO-01.2 use case | mesma mensagem 404 | `U/ConsultarAndamentoTest.java:54` - `hasMessage(outroTitular.getMessage())`; `:56` código `ORDEM_SERVICO_NAO_ENCONTRADA` | ✅ |
| ACO-01.2 API | 404 com corpo idêntico | `H/ConsultaClienteIT.java:98` - `assertThat(outroCpf).isEqualTo(inexistente)` | ✅ |
| ACO-02.1 use case | ordem decrescente com campos do resumo | `U/ListarOrdensServicoTest.java:27` - `itens()).containsExactly(...)` | ✅ |
| ACO-02.1 API | ordem decrescente e campos | `H/AcompanhamentoAdminIT.java:84-88` - ids, `cpfCnpj`, `placa`, `status` | ✅ |
| ACO-02.2 | cliente, veículo, componentes, total e histórico | `U/DetalharOrdemServicoTest.java:22-27`; `H/AcompanhamentoAdminIT.java:98-102` | ✅ |
| ACO-02.3 | inexistente → 404 | `U/DetalharOrdemServicoTest.java:33`; `H/AcompanhamentoAdminIT.java:107` `isNotFound()` | ✅ |
| ACO-03.1 | 60 e 120 min → média 90, quantidade 2 | `U/CalcularTempoMedioExecucaoTest.java:34-35`; `H/AcompanhamentoAdminIT.java:119-120` | ✅ |
| ACO-03.2 | sem finalizadas → 0 e média nula | `U/CalcularTempoMedioExecucaoTest.java:44`; `H/AcompanhamentoAdminIT.java:127-128` | ✅ |
| Domínio ACO-03 | duração aprovação → finalização; vazio se não finalizada | `D/OrdemServicoTest.java:265`, `:274`, `:279-280` | ✅ |
| Sem token → 401 (T8) | 401 | `H/AcompanhamentoAdminIT.java:133-135` | ✅ |

Spec-precision gaps: nenhum.

### Discrimination sensor

Worktree isolado em HEAD, descartado ao final; árvore real limpa antes e depois.

| Mutação | Teste que mata |
| --- | --- |
| M1 `tempoDeExecucao` com `Duration.between(fim, inicio)` | `OrdemServicoTest` - KILLED |
| M2 `ConsultarAndamento` sem filtro de titular | `ConsultarAndamentoTest` - KILLED |
| M3 `CalcularTempoMedioExecucao` divide por 30.0 em vez de 60.0 | `CalcularTempoMedioExecucaoTest` - KILLED |
| M4 sem finalizadas devolve média 0.0 em vez de nula | `CalcularTempoMedioExecucaoTest` - KILLED |
| M5 listagem ordena `abertaEm` crescente | `OrdemServicoRepositoryImplIT` - KILLED |
| M6 `listarFinalizadas` ignora ENTREGUE | `OrdemServicoRepositoryImplIT` - KILLED |
| M7 controller do cliente ignora o `cpfCnpj` informado | `ConsultaClienteIT` - KILLED |

Mutantes sobreviventes: 0.

### Qualidade

Padrões de `Pagina`, `ConsultarServicos` e dos controllers reutilizados; sem SPEC_DEVIATION. Fora da spec: consulta do cliente com CPF/CNPJ de formato inválido responde 422 (T7).

**Gaps**: nenhum.
