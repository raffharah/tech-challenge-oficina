# Validation: notificacao-status

## Validation: PASS

Verificação inline (sem sub-agent, a pedido do usuário). Author = verifier: independência reduzida; evidência por `file:line` e sensor em worktree isolado compensam.

**Diff range**: `7ccdc09..7efc010` + correção do teste `NotificacaoDeStatusImplIT` (sem alteração de código de produção).
**Gate**: `./mvnw verify` exit 0. Surefire 270 testes, Failsafe 187 testes, 0 falhas, 0 erros. JaCoCo 80% ok.

## Spec-anchored check (8/8 ACs)

| AC | Resultado definido na spec | Evidência `file:line` + asserção | Resultado |
| --- | --- | --- | --- |
| 1 | e-mail do titular com OS, placa e `RECEBIDA` | `NotificacaoDeStatusImplIT.java:94-98` `getTo()` contém `maria@oficina.com`; texto contém `Status: RECEBIDA` e `ABC1D23`; `AbrirOrdemServicoTest.java:53-56` notifica a OS `RECEBIDA` | PASS |
| 2 | e-mail em cada novo status | `NotificacaoDeStatusImplIT.java:93-99` 6 envios na ordem dos status; `IniciarDiagnosticoTest.java:36`, `AprovarOrcamentoTest.java:49`, `FinalizarOrdemServicoTest.java:32`, `EntregarVeiculoTest.java:35`, `RejeitarOrcamentoTest.java:34` (`CANCELADA`) | PASS |
| 3 | total do orçamento em `AGUARDANDO_APROVACAO` | `NotificacaoDeStatusImplIT.java:100` contém `Total do orcamento: 135.50`; `:101` ausente em `EM_EXECUCAO`; `ConcluirDiagnosticoTest.java:43-46` | PASS |
| 4 | sem e-mail se a mudança falha | `NotificacaoDeStatusImplIT.java:113` `never().send`; `AprovarOrcamentoTest.java:63`, `ConcluirDiagnosticoTest.java:56`, `IniciarDiagnosticoTest.java:45`, `FinalizarOrdemServicoTest.java:41`, `EntregarVeiculoTest.java:44`, `RejeitarOrcamentoTest.java:43`, `AbrirOrdemServicoTest.java:78` `isEmpty()` | PASS |
| 5 | envio só depois do commit | `NotificacaoDeStatusImplIT.java:126` `containsExactly("RECEBIDA")` lido por conexão separada | PASS |
| 6 | falha de envio: 200, status gravado, log com número da OS | `NotificacaoDeStatusImplIT.falhaDoServidorDeEmailMantem200EStatusGravadoERegistraLogComONumeroDaOs` 200, `$.status`, status no banco e `CapturedOutput` contém `Falha ao enviar e-mail de status da OS <id>` | PASS |
| 7 | cliente sem e-mail: 200, status gravado, sem envio | `NotificacaoDeStatusImplIT.clienteSemEmailNaoRecebeEmailEAMudancaDeStatusSegue` 200, `$.status`, status no banco, `never().send` | PASS |
| 8 | porta no `domain`, impl em `gateway`, sem Spring em `domain`/`application` | `NotificacaoDeStatus.java:3` interface em `domain`; `NotificacaoDeStatusImpl.java:16`; ArchUnit verde no `verify` | PASS |

## Gap achado e corrigido

`envioAcontecePosCommitComOStatusJaGravado` usava o `JdbcTemplate` do teste. MockMvc roda na thread da transação, então o `JdbcTemplate` reutilizava a conexão transacional e via a linha não commitada. O teste não distinguia envio antes e depois do commit (mutante M3 sobreviveu). Correção: consulta por conexão própria (`DriverManagerDataSource` do container). M3 passou a ser morto.

## Discrimination sensor (worktree isolado, removido; porcelain do tree real igual ao baseline)

| Mutante | Teste que mata | Resultado |
| --- | --- | --- |
| M1: remove `notificacao.notificar(salva)` de `IniciarDiagnostico` | `IniciarDiagnosticoTest.iniciaDiagnosticoRegistrandoDataDoClock` | Killed |
| M2: inverte condição do total (`== AGUARDANDO_APROVACAO` → `!=`) | `NotificacaoDeStatusImplIT.cadaMudancaDeStatusEnviaEmail...` | Killed |
| M3: envio imediato (ignora `afterCommit`) | `NotificacaoDeStatusImplIT.envioAcontecePosCommitComOStatusJaGravado` | Survived antes da correção; Killed depois |
| M4: relança `MailException` no lugar do log | `NotificacaoDeStatusImplIT.falhaDoServidorDeEmailMantem200EStatusGravadoERegistraLogComONumeroDaOs` | Killed |
| M5: log sem o número da OS | `NotificacaoDeStatusImplIT.falhaDoServidorDeEmailMantem200EStatusGravadoERegistraLogComONumeroDaOs` | Killed |
| M6: remove o retorno para cliente sem e-mail | `NotificacaoDeStatusImplIT.clienteSemEmailNaoRecebeEmailEAMudancaDeStatusSegue` | Killed |

## Gaps abertos (não bloqueiam)

1. `CANCELADA` coberta só em `application` (`RejeitarOrcamentoTest.java:34`); o IT de gateway não envia a recusa, mas a mensagem independe do status.
2. Envio real pelo Mailpit com `docker compose up` não exercitado.

## Code quality

Sem abstração extra, sem código morto, só os arquivos da feature. Padrão de `gateway` e `BeanConfig` mantido. Diretriz de testes seguida: `AGENTS.md` (TDD, F.I.R.S.T., sufixos `*Test`/`*IT`).
