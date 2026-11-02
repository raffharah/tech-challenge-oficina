# STATE

## Decisions

### AD-001
- **Decision**: O Cliente não é autenticado no MVP; endpoints sob `/cliente`, sem JWT, simulam a resposta do cliente (aprovar ou rejeitar orçamento) e expõem a consulta de andamento.
- **Reason**: JWT é exigido só nas APIs administrativas e a resposta real do cliente não é necessária no MVP.
- **Trade-off**: Sem login, quem conhece o número da OS e o CPF/CNPJ do titular consegue consultar e decidir. Consulta, aprovação e rejeição exigem número da OS + CPF/CNPJ coincidentes; OS de outro titular responde 404 igual a OS inexistente, para não expor dados por enumeração.
- **Scope**: decisao-orcamento, acompanhamento-os; `infrastructure/http` e `config/SecurityConfig`.
- **Date**: 2026-10-26
- **Revised**: 2026-11-02. A listagem `GET /cliente/ordens-servico?cpfCnpj=` exige só o CPF/CNPJ: quem conhece o documento de um cliente vê o resumo (número, placa, status, total) de todas as OS dele. Aceito no MVP pelo requisito de o cliente acompanhar as próprias OS sem login; titular sem OS recebe página vazia, igual a documento sem cadastro. A autenticação do cliente fecha esse trade-off.
- **Revised**: 2026-11-01. Antes, aprovar e rejeitar exigiam só o número da OS: qualquer chamador podia decidir pela OS de outro cliente percorrendo números sequenciais, e a resposta expunha CPF/CNPJ e placa que a consulta protege. A decisão passou a exigir o CPF/CNPJ do titular, como a consulta.
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

### AD-006
- **Decision**: Listagens de clientes e veículos são uma única operação paginada (`GET /clientes`, `GET /veiculos`) com filtro opcional por CPF/CNPJ ou placa; o filtro sem resultado responde 200 com página vazia. A busca com 404 continua nos casos de uso, usada pela porta `CadastroDeVeiculos` ao abrir a OS.
- **Reason**: A busca e a listagem dividiam o mesmo caminho via `params`; o OpenAPI não admite duas operações iguais e o springdoc as fundia, marcando o filtro como obrigatório no Swagger.
- **Trade-off**: Quem filtrava por CPF/CNPJ ou placa e tratava 404 passa a tratar página vazia.
- **Scope**: clientes-veiculos, ajustes-revisao.
- **Date**: 2026-11-02
- **Status**: active

### AD-007
- **Decision**: A OS nasce `RECEBIDA` e vai para `EM_DIAGNOSTICO` só pelo comando do funcionário `POST /ordens-servico/{id}/diagnostico`.
- **Reason**: Com a transição automática na abertura, os dois status tinham o mesmo instante e `RECEBIDA` nunca era observável; a revisão apontou que o status não representava uma etapa real.
- **Trade-off**: Uma chamada a mais no fluxo; incluir serviço ou item em OS `RECEBIDA` responde `422 ORDEM_FORA_DE_DIAGNOSTICO`.
- **Scope**: abertura-os-orcamento, ajustes-revisao; substitui a política "Sistema atualiza a OS para em diagnóstico" do fluxo da OS.
- **Date**: 2026-11-02
- **Status**: active

## Handoff

- **Feature**: `ajustes-revisao` (Implemented); escopos 1 a 8 implementados
- **Phase / Task**: Validate concluído (`.specs/features/ajustes-revisao/validation.md`)
- **Completed**: REV-01 a REV-06
- **In-progress** (file:line): none
- **Next step**: Atualizar o board do Figma e `fluxo-os.png` com o comando "Inicia diagnóstico"; abrir PR de `fix/ajustes-revisao-fase1`.
- **Blockers**: none
- **Uncommitted files**: none
- **Branch**: fix/ajustes-revisao-fase1
