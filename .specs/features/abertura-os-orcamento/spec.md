# Abertura da OS e Orçamento Specification

## Problem Statement

O atendimento e o orçamento são feitos em anotações e planilhas. O funcionário precisa abrir a OS a partir do cliente e do veículo identificados, registrar serviços, peças e insumos no diagnóstico e enviar ao cliente um orçamento calculado automaticamente (fluxo-os 3.1 e 3.2, passos 6 a 11).

## Goals

- [ ] OS nasce Recebida, passa a Em diagnóstico e chega a Aguardando aprovação com orçamento enviado ao cliente.
- [ ] Orçamento = Σ valor padrão dos serviços + Σ (quantidade × preço de venda) dos itens, gravado como versão 1 do histórico.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Cadastro de cliente ou veículo dentro da abertura | Feito antes pelo CRUD (escopo `clientes-veiculos`), conforme o desvio "Cadastra cliente/veículo" |
| Decisão do cliente sobre o orçamento | Escopo `decisao-orcamento` |
| Reserva de itens | Só na aprovação (Decisões do MVP) |
| Desconto, impostos e mão de obra separada | Orçamento é somatório simples (fluxo-os 3.2) |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Dados da abertura | CPF/CNPJ do cliente, placa do veículo e descrição do pedido | Fluxo-os 3.1; descrição registra o "Pedido de serviço" | n |
| Veículo de outro cliente | Abertura rejeitada com 422 | Veículo é do cliente que solicita | n |
| Recebida → Em diagnóstico | Automática, na mesma operação de abertura; histórico registra as duas transições | Política "O Sistema atualiza a OS para em diagnóstico" (AD-002) | n |
| Preço do componente | Copiado no momento da inclusão (valor padrão do serviço, preço de venda do item) | Mudanças no catálogo não alteram OS em andamento | n |
| Falta de saldo na inclusão | Inclusão aceita, item sinalizado como indisponível, funcionário notificado | AD-008 | y |
| Mesmo item incluído duas vezes | Quantidades somadas em um único componente | Evita linhas duplicadas no orçamento | n |
| Prazo | Data informada pelo funcionário ao enviar o orçamento; deve ser posterior à data atual | Decisões do MVP: prazo manual | n |
| Remoção de cadastros usados | Veículo, cliente, serviço ou item referenciado por OS não é removido (409) | Preserva histórico | n |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Abrir OS ⭐ MVP

**User Story**: Como funcionário, quero abrir uma OS para o cliente e o veículo identificados.

**Why P1**: Início de todo o fluxo.

**Acceptance Criteria**:

1. WHEN o funcionário abre uma OS com CPF/CNPJ de cliente ATIVO e placa de veículo desse cliente THEN o sistema SHALL criar a OS com status RECEBIDA, mudá-la para EM_DIAGNOSTICO, registrar as duas transições com data/hora e responder 201 com o número da OS.
2. IF o CPF/CNPJ é inválido ou a placa não segue os padrões THEN o sistema SHALL responder 422 sem criar a OS.
3. IF o cliente ou o veículo não está cadastrado THEN o sistema SHALL responder 404 sem criar a OS.
4. IF o cliente está SUSPENSO THEN o sistema SHALL rejeitar o pedido com 422 e mensagem de cliente suspenso por inadimplência.
5. IF o veículo pertence a outro cliente THEN o sistema SHALL responder 422 sem criar a OS.

**Independent Test**: Abrir OS para cliente ativo (201, EM_DIAGNOSTICO) e para cliente suspenso (422).

---

### P1: Registrar diagnóstico ⭐ MVP

**User Story**: Como mecânico, quero adicionar e remover serviços, peças e insumos da OS durante o diagnóstico.

**Why P1**: Funcionalidades obrigatórias "inclusão dos serviços" e "inclusão de peças e insumos".

**Acceptance Criteria**:

1. WHILE a OS está EM_DIAGNOSTICO, WHEN o mecânico adiciona um serviço ATIVO do catálogo THEN o sistema SHALL incluí-lo com o valor padrão vigente.
2. WHILE a OS está EM_DIAGNOSTICO, WHEN o mecânico adiciona uma peça ou insumo ATIVO com quantidade positiva THEN o sistema SHALL incluí-lo com o preço de venda e a disponibilidade retornados pela porta `EstoqueDeItens`.
3. WHEN um item incluído está indisponível THEN o sistema SHALL marcar o componente como indisponível e notificar o funcionário com SKU e quantidade.
4. WHILE a OS está EM_DIAGNOSTICO, WHEN o mecânico remove um componente THEN o sistema SHALL retirá-lo da OS.
5. IF o serviço ou item está INATIVO ou não existe THEN o sistema SHALL responder 422.
6. IF a OS não está EM_DIAGNOSTICO THEN o sistema SHALL rejeitar inclusão e remoção com 422.

**Independent Test**: Adicionar troca de óleo + 4 litros de óleo e removê-los; tentar adicionar em OS fora de diagnóstico (422).

---

### P1: Calcular e enviar orçamento ⭐ MVP

**User Story**: Como mecânico, quero gerar o orçamento automaticamente e enviá-lo ao cliente.

**Why P1**: Funcionalidades obrigatórias "geração automática" e "envio para aprovação".

**Acceptance Criteria**:

1. WHILE a OS está EM_DIAGNOSTICO, WHEN o mecânico envia o orçamento com prazo posterior à data atual THEN o sistema SHALL calcular o total como Σ valor dos serviços + Σ (quantidade × preço) dos itens e registrar o orçamento no histórico com versão sequencial e situação ENVIADO.
2. WHEN o orçamento é registrado THEN o sistema SHALL enviar ao e-mail do cliente os componentes, os valores, o total e o prazo, e mudar a OS para AGUARDANDO_APROVACAO.
3. IF a OS não tem nenhum serviço THEN o sistema SHALL responder 422 sem calcular.
4. IF o prazo é igual ou anterior à data atual THEN o sistema SHALL responder 422.
5. The sistema SHALL calcular valores monetários em `BigDecimal` com duas casas decimais e arredondamento HALF_EVEN.

**Independent Test**: Serviço R$ 100,00 + 2 itens de R$ 35,50: total R$ 171,00, OS em AGUARDANDO_APROVACAO, e-mail registrado.

---

### P1: Transições válidas ⭐ MVP

**User Story**: Como sistema, quero aceitar só as transições do ciclo de vida da OS.

**Why P1**: Requisito "aplicar regras de transição".

**Acceptance Criteria**:

1. The sistema SHALL permitir somente as transições RECEBIDA→EM_DIAGNOSTICO, EM_DIAGNOSTICO→AGUARDANDO_APROVACAO, AGUARDANDO_APROVACAO→EM_EXECUCAO, AGUARDANDO_APROVACAO→EM_DIAGNOSTICO, AGUARDANDO_APROVACAO→CANCELADA, EM_EXECUCAO→EM_DIAGNOSTICO, EM_EXECUCAO→FINALIZADA, FINALIZADA→ENTREGUE e FINALIZADA→CANCELADA.
2. IF uma transição fora dessa lista é pedida THEN o sistema SHALL lançar `DomainException` sem alterar o status.
3. WHEN uma transição acontece THEN o sistema SHALL registrar status anterior, novo status e data/hora no histórico da OS.

**Independent Test**: Teste de domínio parametrizado com todas as combinações de status.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| OS-01 | P1: Abrir OS | - | Pending |
| OS-02 | P1: Registrar diagnóstico | - | Pending |
| OS-03 | P1: Calcular e enviar orçamento | - | Pending |
| OS-04 | P1: Transições válidas | - | Pending |

**Coverage:** 4 total, 0 mapped tasks, 4 unmapped ⚠️

---

## Success Criteria

- [ ] Fluxo abertura → diagnóstico → orçamento enviado demonstrável pelo Swagger com uma OS.
