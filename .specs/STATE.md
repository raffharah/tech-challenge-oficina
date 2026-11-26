# STATE

## Decisions

### AD-001
- **Decision**: O Cliente não é autenticado no MVP; endpoints sob `/cliente`, sem JWT, simulam a resposta do cliente (aprovar ou rejeitar orçamento) e expõem a consulta de andamento.
- **Reason**: JWT é exigido só nas APIs administrativas e a resposta real do cliente não é necessária no MVP.
- **Trade-off**: Sem login, quem conhece o número da OS e o CPF/CNPJ do titular consegue consultar e decidir. Consulta, aprovação e rejeição exigem número da OS + CPF/CNPJ coincidentes; OS de outro titular responde 404 igual a OS inexistente, para não expor dados por enumeração.
- **Scope**: decisao-orcamento, acompanhamento-os; `infrastructure/http` e `config/SecurityConfig`.
- **Date**: 2026-10-26
- **Revised**: 2026-11-26 (AD-010). Aprovar e rejeitar viram um endpoint, `POST /cliente/ordens-servico/{id}/decisao`, com a mesma identificação por CPF/CNPJ.
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
- **Revised**: 2026-11-26. A fase 2 (`docs/fase2-tech-challenge.md`) amplia o escopo. Os requisitos obrigatórios dela entram como evolução, sem remover o que a fase 1 entregou.
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
- **Revised**: 2026-11-26 (AD-011). A OS nasce `RECEBIDA` já com os serviços e itens solicitados na abertura. A regra acima vale para inclusões e remoções depois da abertura.
- **Scope**: abertura-os-orcamento, ajustes-revisao; substitui a política "Sistema atualiza a OS para em diagnóstico" do fluxo da OS.
- **Date**: 2026-11-02
- **Status**: active

### AD-008
- **Decision**: Todo recurso inexistente responde 404, venha ele do caminho ou de uma referência no corpo da requisição (`clienteId`, `servicoId`, `itemId`, cliente e veículo na abertura da OS). 422 fica para regra de negócio violada.
- **Reason**: A API respondia 404 para cliente e veículo inexistentes, mas 422 para serviço ou item inexistente incluído na OS; a regra única deixa o tratamento de erro previsível. Escolhida a regra que já valia na maioria dos casos e fora do fluxo principal.
- **Trade-off**: O gateway `EstoqueDeItensImpl` deixa de converter `ITEM_NAO_ENCONTRADO` em 422 na cotação; a conversão segue só na baixa, onde item sumido é inconsistência interna e não referência do chamador.
- **Scope**: abertura-os-orcamento, ajustes-revisao.
- **Date**: 2026-11-02
- **Status**: active

### AD-009
- **Decision**: A listagem da oficina (`GET /ordens-servico`) ordena por status (`EM_EXECUCAO`, `AGUARDANDO_APROVACAO`, `EM_DIAGNOSTICO`, `RECEBIDA`, `CANCELADA`), depois pela abertura mais antiga e pelo `id`, e omite `FINALIZADA` e `ENTREGUE`. A ordem fica na consulta do repositório.
- **Reason**: Requisito da fase 2 (`docs/fase2-tech-challenge.md:30`). `CANCELADA` continua na listagem: retirá-la seria regressão da fase 1. A ordem precisa estar no banco para a paginação funcionar.
- **Trade-off**: A prioridade dos status fica escrita na query, e não no domínio. A listagem do cliente segue da mais recente para a mais antiga, com todos os status.
- **Scope**: listagem-priorizada; `infrastructure/db/ordemservico`.
- **Date**: 2026-11-26
- **Status**: active

### AD-010
- **Decision**: A decisão do orçamento chega por um único endpoint, `POST /cliente/ordens-servico/{id}/decisao`, com `{cpfCnpj, decisao: APROVADO | RECUSADO}`. As rotas `/aprovacao` e `/rejeicao` são removidas. A identificação continua pelo CPF/CNPJ do titular (AD-001).
- **Reason**: A fase 2 pede "endpoint para receber notificações externas de aprovação ou recusa" (`docs/fase2-tech-challenge.md:29`). Webhook com token não está na documentação.
- **Trade-off**: Quem chamava as rotas antigas passa a usar a nova.
- **Scope**: endpoint-decisao-orcamento; `infrastructure/http/ordemservico`.
- **Date**: 2026-11-26
- **Status**: active

### AD-011
- **Decision**: A abertura da OS recebe `cpfCnpj`, `email`, `placa`, `servicos` (ao menos 1) e `itens` (ao menos 1). A OS nasce `RECEBIDA` com os serviços e itens solicitados. O `email` é obrigatório e grava ou substitui o e-mail do cliente. Cliente e veículo precisam estar cadastrados. Revisa AD-007: incluir e remover componentes depois da abertura continua exigindo `EM_DIAGNOSTICO`.
- **Reason**: A fase 2 pede abertura com dados do cliente, veículo, serviços e peças (`docs/fase2-tech-challenge.md:27`). No diagnóstico, o mecânico ajusta o que mais for necessário.
- **Trade-off**: O corpo antigo (`cpfCnpj` + `placa`) deixa de existir. `cliente.email` aceita nulo, porque o cliente pode ser cadastrado antes de abrir a primeira OS.
- **Scope**: abertura-os-completa; `domain/ordemservico`, `domain/cliente`, migration `V5`.
- **Date**: 2026-11-26
- **Status**: active

### AD-012
- **Decision**: Toda mudança de status da OS, inclusive a abertura, envia e-mail ao titular por uma porta `NotificacaoDeStatus` do domínio. A implementação fica em `infrastructure/gateway`, envia só depois do commit e registra a falha de envio em log, sem desfazer a mudança.
- **Reason**: A fase 2 pede a atualização de status da OS por uma ferramenta como e-mail (`docs/fase2-tech-challenge.md:34`). E-mail de mudança que sofreu rollback informaria um status falso.
- **Trade-off**: Sem fila nem reenvio: e-mail que falhou se perde. Revisa a decisão "Sistemas externos" de `docs/ddd/linguagem-ubiqua.md`.
- **Scope**: notificacao-status; casos de uso de transição de status.
- **Date**: 2026-11-26
- **Status**: active

## Handoff

- **Feature**: fase 2, APIs: `listagem-priorizada`, `endpoint-decisao-orcamento`, `abertura-os-completa`, `notificacao-status`
- **Phase / Task**: Specify concluído; design e tasks em revisão
- **Completed**: specs das quatro features; AD-009 a AD-012
- **In-progress** (file:line): none
- **Next step**: aprovar design e tasks; atualizar as Decisões do MVP em `docs/ddd/linguagem-ubiqua.md`; executar na ordem listagem → decisão → abertura → notificação
- **Blockers**: none
- **Uncommitted files**: `docs/fase2-tech-challenge.md`, `AGENTS.md`, `.specs/STATE.md`, `.specs/features/{listagem-priorizada,endpoint-decisao-orcamento,abertura-os-completa,notificacao-status}/`
- **Branch**: feature/api
