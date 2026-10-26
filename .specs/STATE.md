# STATE

## Decisions

### AD-001
- **Decision**: O Cliente não é autenticado no MVP; endpoints sob `/cliente`, sem JWT, simulam a resposta do cliente (aprovar ou rejeitar orçamento) e expõem a consulta de andamento.
- **Reason**: JWT é exigido só nas APIs administrativas e a resposta real do cliente não é necessária no MVP.
- **Trade-off**: Qualquer chamador pode registrar a decisão do cliente; a consulta exige número da OS + CPF/CNPJ coincidentes para não expor dados por enumeração.
- **Scope**: decisao-orcamento, acompanhamento-os; `infrastructure/http` e `config/SecurityConfig`.
- **Date**: 2026-10-26
- **Status**: active

### AD-002
- **Decision**: Políticas automáticas (mudança de status, baixa de estoque) executam de forma síncrona dentro do caso de uso que as dispara, na transação aberta na entrada.
- **Reason**: Monolito único; consistência imediata entre OS e Estoque sem mensageria.
- **Trade-off**: Acoplamento temporal entre contextos via porta `EstoqueDeItens`.
- **Scope**: todos os casos de uso de `application`.
- **Date**: 2026-10-26
- **Status**: active

### AD-003
- **Decision**: O escopo do MVP é limitado às funcionalidades e requisitos técnicos obrigatórios de `docs/fase1-tech-challenge.md`; trilhas do Event Storming sem item obrigatório correspondente ficam fora (lista em `.specs/ROADMAP.md`).
- **Reason**: Entregar o obrigatório com qualidade antes de fluxos de exceção.
- **Trade-off**: Diverge das Decisões do MVP atuais em `docs/ddd/linguagem-ubiqua.md`, que precisam ser atualizadas.
- **Scope**: todas as features.
- **Date**: 2026-10-26
- **Status**: active

### AD-004
- **Decision**: Sem Reserva: a aprovação do orçamento verifica o saldo físico e dá baixa direta dos itens, tudo ou nada.
- **Reason**: Sem cancelamento após a aprovação no escopo, reserva e consumo em dois passos não têm cenário que os diferencie.
- **Trade-off**: Volta a precisar de saldo reservado quando existir cancelamento após aprovação ou serviço adicional.
- **Scope**: estoque-itens, decisao-orcamento.
- **Date**: 2026-10-26
- **Status**: active

### AD-005
- **Decision**: Tempo de execução de uma OS = data/hora de FINALIZADA − data/hora de EM_EXECUCAO, lidas do histórico de status.
- **Reason**: Atende "tempo médio de execução dos serviços" sem registrar início e conclusão por serviço.
- **Trade-off**: Sem tempo médio por tipo de serviço.
- **Scope**: execucao-servicos, acompanhamento-os.
- **Date**: 2026-10-26
- **Status**: active

## Handoff

- **Feature**: nenhuma iniciada; ver `.specs/ROADMAP.md`
- **Phase / Task**: Specify, Design e Tasks concluídos para os 8 escopos (Draft); próximo é Execute de `fundacao-plataforma` T1
- **Completed**: none
- **In-progress** (file:line): none
- **Next step**: Aprovar tasks, atualizar Decisões do MVP em `docs/ddd/linguagem-ubiqua.md` (AD-003/AD-004) e executar `fundacao-plataforma`.
- **Blockers**: none
- **Uncommitted files**: `.specs/`
- **Branch**: feat/documentacao-inicial
