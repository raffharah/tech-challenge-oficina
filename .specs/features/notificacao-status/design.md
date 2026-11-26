# Notificação de Status por E-mail Design

**Spec**: `.specs/features/notificacao-status/spec.md`
**Decisões**: AD-012, AD-002
**Depende de**: `abertura-os-completa` (`cliente.email` e `ClienteDTO.email`)

## Fluxo

```
Controller (@Transactional)
  └─ caso de uso de transição
       ordem.<transição>(agora)
       salva = ordens.salvar(ordem)
       notificacao.notificar(salva)              ← porta do domínio
            │
            ▼
       NotificacaoDeStatusImpl (infrastructure/gateway/notificacao)
         1. busca o e-mail do titular (ConsultarClientes.buscarPorCpfCnpj), dentro da transação
         2. monta a mensagem (OS, placa, status; total em AGUARDANDO_APROVACAO)
         3. TransactionSynchronization.afterCommit → envia (toda entrada abre transação)
         4. MailException → log.error com o número da OS; nada é propagado
```

Exceção antes do commit (regra violada, `SALDO_INSUFICIENTE`, erro de banco) faz `afterCommit` não rodar. Assim, nenhum e-mail sai (NTF-01.4, NTF-01.5).

## Domínio

- `domain/ordemservico/NotificacaoDeStatus` (porta nova): `void notificar(OrdemServico ordem)`.

## Application

Os 7 casos de uso que mudam status recebem a porta no construtor e chamam `notificacao.notificar(salva)` depois de `ordens.salvar`:

| Caso de uso | Status notificado |
| --- | --- |
| `AbrirOrdemServico` | `RECEBIDA` |
| `IniciarDiagnostico` | `EM_DIAGNOSTICO` |
| `ConcluirDiagnostico` | `AGUARDANDO_APROVACAO` |
| `AprovarOrcamento` | `EM_EXECUCAO` |
| `RejeitarOrcamento` | `CANCELADA` |
| `FinalizarOrdemServico` | `FINALIZADA` |
| `EntregarVeiculo` | `ENTREGUE` |

- Cada caso de uso faz uma chamada explícita. Detectar a mudança no repositório esconderia a regra na persistência.
- Teste: `NotificacaoDeStatusEmMemoria`, em `src/test/.../application/ordemservico`, registra as OS notificadas. Cada teste de transição confere a notificação e, no caminho de erro, a ausência dela.

## Infraestrutura

- `pom.xml`: `spring-boot-starter-mail`.
- `infrastructure/gateway/notificacao/NotificacaoDeStatusImpl` (`@Component`): usa `JavaMailSender`, `SimpleMailMessage`, `ConsultarClientes` e `TransactionSynchronizationManager`.
- `application.yml`:
  - `spring.mail.host: ${MAIL_HOST:localhost}`
  - `spring.mail.port: ${MAIL_PORT:1025}`
  - `oficina.notificacao.remetente: ${NOTIFICACAO_REMETENTE:oficina@oficina.local}`
- `docker-compose.yml`:
  - serviço `mailpit` (`axllent/mailpit`), com SMTP 1025 e interface web 8025;
  - `app` recebe `MAIL_HOST: mailpit`.
- `BeanConfig`: a porta entra nos 7 beans.

## Testes de integração

- `NotificacaoDeStatusImplIT`:
  - `@MockitoBean JavaMailSender`;
  - uma OS levada de `RECEBIDA` a `ENTREGUE` pela API envia 6 mensagens (sem `CANCELADA`), com destinatário, status e total em `AGUARDANDO_APROVACAO`;
  - aprovação com `SALDO_INSUFICIENTE` não envia;
  - `JavaMailSender` lançando `MailSendException` mantém a resposta 200 e o status gravado.
- O mock evita uma dependência de teste nova (GreenMail ou contêiner Mailpit).

## Documentação

- `docs/ddd/linguagem-ubiqua.md`, Decisões do MVP: "Sistemas externos" passa a dizer que o e-mail está no escopo, só para a notificação de status.
- `docs/ddd/c4/`: o e-mail entra como sistema externo no diagrama de contexto e de container.
