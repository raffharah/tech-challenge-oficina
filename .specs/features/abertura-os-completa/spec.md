# Abertura de OS Completa Specification

## Problem Statement

A fase 2 pede que a abertura da OS receba os dados do cliente, do veículo, dos serviços e das peças e devolva a identificação única da OS (`docs/fase2-tech-challenge.md:27`). Hoje `POST /ordens-servico` recebe só `cpfCnpj` e `placa`. Os serviços e itens entram depois, um por chamada, e só com a OS em `EM_DIAGNOSTICO` (AD-007).

## Goals

- [ ] ABR-01: uma chamada abre a OS com CPF/CNPJ, e-mail, placa, serviços e peças/insumos solicitados.
- [ ] ABR-02: o e-mail informado na abertura fica registrado no cliente.
- [ ] ABR-03: a resposta traz o número da OS.
- [ ] ABR-04: abertura inválida não grava nada.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Cadastrar cliente ou veículo na abertura | Os dados pedidos na abertura são CPF/CNPJ, e-mail e placa; cadastro tem rota própria |
| Identificador UUID ou código alfanumérico | O `id` da OS já é único; o requisito não pede formato |
| Baixa de estoque na abertura | AD-004: baixa só na aprovação |
| E-mail no seed | Fica para depois, por decisão do usuário |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Corpo da abertura | `{"cpfCnpj", "email", "placa", "servicos": [servicoId], "itens": [{"itemId", "quantidade"}]}` | Decisão do usuário | y |
| Status com componentes na abertura | A OS nasce `RECEBIDA` com os serviços e itens solicitados; no diagnóstico o mecânico inclui ou remove o que mais for necessário | Decisão do usuário; preserva AD-007 | y |
| E-mail | Obrigatório na abertura; grava ou substitui o e-mail do cliente | Decisão do usuário: e-mail é dado principal do cliente, cadastrado na abertura | y |
| Coluna de e-mail | Migration nova `V5` adiciona `cliente.email` aceitando nulo | `AGENTS.md`: schema muda só por migration nova; cliente cadastrado sem OS aberta ainda não tem e-mail | y |
| Serviços e itens | Listas obrigatórias, com ao menos 1 serviço e 1 item | Decisão do usuário: fazem parte da solicitação da abertura | y |
| Corpo antigo (`cpfCnpj` + `placa`) | Substituído pelo corpo novo | Não manter dois contratos para a mesma operação | y |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Abrir OS com os dados da solicitação ⭐ MVP

**User Story**: Como funcionário, quero abrir a OS numa única chamada com o cliente, o veículo e os serviços e peças solicitados, para registrar o atendimento de uma vez.

**Why P1**: Requisito obrigatório da fase 2.

**Acceptance Criteria**:

1. WHEN o funcionário envia `cpfCnpj`, `email`, `placa`, `servicos` e `itens` THEN o sistema SHALL abrir a OS `RECEBIDA` com esses serviços e itens e responder 201 com o `id` da OS.
2. WHEN a OS é aberta THEN o sistema SHALL gravar o `email` informado no cliente do CPF/CNPJ, substituindo o anterior.
3. The sistema SHALL copiar o valor do serviço e o preço de venda do item no momento da abertura, como a inclusão faz hoje.
4. The sistema SHALL manter o `total` nulo até a geração do orçamento, como hoje.
5. WHEN a OS em `EM_DIAGNOSTICO` recebe inclusão ou remoção de serviço ou item THEN o sistema SHALL aplicar a mudança sobre os componentes da abertura.
6. WHILE a OS está `RECEBIDA`, WHEN chega inclusão ou remoção de serviço ou item THEN o sistema SHALL responder 422 `ORDEM_FORA_DE_DIAGNOSTICO`.
7. WHEN o mesmo item aparece duas vezes em `itens` THEN o sistema SHALL somar as quantidades, como a inclusão faz hoje.

**Independent Test**: Abrir OS com 1 serviço e 1 item; conferir OS `RECEBIDA` com os componentes, e-mail no cliente e estoque sem baixa; iniciar o diagnóstico e incluir outro serviço.

---

### P1: Rejeitar abertura inconsistente ⭐ MVP

**User Story**: Como funcionário, quero que a abertura com dados inválidos não grave nada pela metade.

**Why P1**: Sem atomicidade, um erro em um item deixa o e-mail do cliente alterado sem OS aberta.

**Acceptance Criteria**:

1. IF o CPF/CNPJ ou a placa não estão cadastrados THEN o sistema SHALL responder 404 `CLIENTE_NAO_ENCONTRADO` ou `VEICULO_NAO_ENCONTRADO`.
2. IF a placa é de outro cliente THEN o sistema SHALL responder 422 `VEICULO_DE_OUTRO_CLIENTE`.
3. IF um serviço ou item não existe THEN o sistema SHALL responder 404 `SERVICO_NAO_ENCONTRADO` ou `ITEM_NAO_ENCONTRADO` (AD-008).
4. IF o `email` não tem formato de e-mail THEN o sistema SHALL responder 422 `EMAIL_INVALIDO`.
5. IF CPF/CNPJ, placa ou quantidade são inválidos THEN o sistema SHALL responder 422 `CPF_CNPJ_INVALIDO`, `PLACA_INVALIDA` ou `QUANTIDADE_INVALIDA`.
6. IF o mesmo serviço aparece duas vezes THEN o sistema SHALL responder 422 `SERVICO_JA_INCLUIDO`.
7. IF `cpfCnpj`, `email` ou `placa` falta, ou `servicos` ou `itens` falta ou vem vazio, THEN o sistema SHALL responder 400.
8. IF a abertura falha THEN o sistema SHALL não gravar a OS nem alterar o e-mail do cliente.

**Independent Test**: Abrir com serviço inexistente: 404, nenhuma OS gravada, e-mail do cliente inalterado.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| ABR-01 | P1: Abrir OS com os dados da solicitação | - | Pending |
| ABR-02 | P1: Abrir OS com os dados da solicitação | - | Pending |
| ABR-03 | P1: Abrir OS com os dados da solicitação | - | Pending |
| ABR-04 | P1: Rejeitar abertura inconsistente | - | Pending |

**Coverage:** 4 total, 0 mapped to tasks, 4 unmapped

---

## Success Criteria

- [ ] `OrdemServicoAberturaIT` cobre abertura com componentes, gravação do e-mail e falha sem resíduo.
- [ ] AD-007 revisada em `.specs/STATE.md`: a OS nasce `RECEBIDA` com os componentes solicitados.
