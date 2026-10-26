# STATE

## Decisions

### AD-001
- **Decision**: O Cliente não é autenticado no MVP; endpoints sob `/cliente`, sem JWT, simulam a resposta do cliente (decisão sobre orçamento, pagamento) e a consulta de andamento.
- **Reason**: O requisito exige JWT só nas APIs administrativas e a resposta real do cliente não é necessária no MVP; o fluxo segue com envio e resposta simulados.
- **Trade-off**: Qualquer chamador pode registrar a decisão do cliente; a consulta de andamento exige número da OS + CPF/CNPJ coincidentes para não expor dados por enumeração.
- **Scope**: decisao-orcamento, servico-adicional, cobranca-entrega, acompanhamento-os; `infrastructure/http` e `config/SecurityConfig`.
- **Date**: 2026-10-26
- **Status**: active

### AD-002
- **Decision**: Políticas automáticas do Event Storming ("O Sistema atualiza a OS para X", notificações, reserva, consumo) executam de forma síncrona dentro do caso de uso que dispara o evento, na mesma transação aberta na entrada.
- **Reason**: Monolito único; consistência imediata entre OS, Estoque e Cobrança sem infraestrutura de mensageria.
- **Trade-off**: Acoplamento temporal entre contextos (via portas do domínio); migrar para eventos assíncronos exige reescrever a orquestração em `application`.
- **Scope**: todos os casos de uso de `application`.
- **Date**: 2026-10-26
- **Status**: active

### AD-003
- **Decision**: Não existe aprovação parcial com reserva. Recusa parcial rejeita o orçamento inicial e abre uma única renegociação; a reserva acontece só na aprovação integral do orçamento vigente.
- **Reason**: Fluxo da OS (3.3) e Decisões do MVP ("Reserva só na aprovação"); o "aprovado parcialmente" do fluxo de estoque (6.1) é descartado.
- **Trade-off**: O cliente não aprova parte do orçamento e executa o resto sem nova rodada.
- **Scope**: decisao-orcamento, estoque-itens.
- **Date**: 2026-10-26
- **Status**: active

### AD-004
- **Decision**: Atraso é atributo da OS, não status: rotina agendada diária marca a OS como atrasada quando o prazo do orçamento vigente passa sem a OS estar Finalizada, e notifica o cliente uma única vez por prazo.
- **Reason**: Glossário (prazo único da OS) e Decisões do MVP (rotina diária); o "serviço atrasado" do fluxo seria um oitavo status fora da lista obrigatória.
- **Trade-off**: Sem atraso por serviço individual.
- **Scope**: execucao-servicos, servico-adicional, acompanhamento-os.
- **Date**: 2026-10-26
- **Status**: active

### AD-005
- **Decision**: Dias úteis = segunda a sexta, sem calendário de feriados; data e hora entram por `Clock` injetado.
- **Reason**: Prazos de atraso e vencimento do pagamento (5 dias úteis) precisam ser determinísticos e testáveis.
- **Trade-off**: Feriados contam como dia útil.
- **Scope**: inadimplencia, execucao-servicos.
- **Date**: 2026-10-26
- **Status**: active

### AD-006
- **Decision**: Orçamentos da OS são versionados (histórico de orçamentos); o orçamento vigente é a última versão aprovada ou em análise. Serviço adicional aceito reserva só o acréscimo de itens; rejeitado retoma a versão anterior sem reserva e sem cancelar a OS.
- **Reason**: Fluxo da OS 4.2 ("Obtém o orçamento anterior do histórico") e Decisões do MVP ("Serviço adicional rejeitado não reserva nada").
- **Trade-off**: Modelo de Orçamento com versão e situação, mais complexo que um valor único na OS.
- **Scope**: abertura-os-orcamento, decisao-orcamento, servico-adicional, cobranca-entrega.
- **Date**: 2026-10-26
- **Status**: active

### AD-007
- **Decision**: No cancelamento por inadimplência, peças recuperadas voltam ao saldo físico como entrada; peças não recuperadas só registram perda ou avaria; insumos não voltam.
- **Reason**: O consumo já baixou o saldo físico na finalização; baixar de novo na perda (fluxo de estoque 7.2) contaria duas vezes.
- **Trade-off**: Perda não altera saldo, só gera registro.
- **Scope**: inadimplencia, estoque-itens.
- **Date**: 2026-10-26
- **Status**: active

### AD-008
- **Decision**: Incluir peça ou insumo sem saldo disponível suficiente na OS em diagnóstico é aceito e sinalizado (indisponível + notificação ao funcionário); o bloqueio acontece só na reserva, na aprovação.
- **Reason**: Decisão do usuário; mantém o orçamento completo enquanto a entrada de estoque é providenciada.
- **Trade-off**: O cliente pode receber orçamento com item ainda sem saldo.
- **Scope**: abertura-os-orcamento, estoque-itens.
- **Date**: 2026-10-26
- **Status**: active

## Handoff

- **Feature**: nenhuma iniciada; ver `.specs/ROADMAP.md`
- **Phase / Task**: Specify concluído para os 11 escopos; próximo é Design de `fundacao-plataforma`
- **Completed**: none
- **In-progress** (file:line): none
- **Next step**: Confirmar as specs e iniciar Design/Tasks de `.specs/features/fundacao-plataforma`.
- **Blockers**: none
- **Uncommitted files**: `.specs/`
- **Branch**: feat/documentacao-inicial
