# Clientes e Veículos Specification

## Problem Statement

A oficina perde o histórico de clientes e veículos e não valida CPF/CNPJ nem placa. A abertura da OS (fluxo-os 3.1) depende de identificar o cliente pelo CPF/CNPJ, saber se ele está suspenso e identificar o veículo pela placa.

## Goals

- [ ] CRUD de clientes e veículos com CPF/CNPJ e placa validados no objeto de valor.
- [ ] Situação do cliente (ativo/suspenso) disponível para a abertura da OS e a inadimplência.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Consulta à Receita Federal | Validação é por formato e dígitos verificadores (glossário) |
| Alterar CPF/CNPJ de cliente cadastrado | Identificador fiscal único; correção = novo cadastro |
| Transferência de veículo entre clientes | Não aparece nos fluxos |
| Suspensão automática | Pertence ao escopo `inadimplencia`; aqui só a operação de domínio |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Dados do cliente | Nome, CPF/CNPJ, e-mail obrigatórios; telefone opcional | E-mail é o canal de todas as notificações | n |
| Armazenamento de CPF/CNPJ | Só dígitos; aceita entrada com ou sem máscara | Unicidade não depende de formatação | n |
| Armazenamento da placa | Maiúsculas sem hífen (ABC1234, ABC1D23); aceita entrada com hífen e minúsculas | Unicidade não depende de formatação | n |
| Ano do veículo | Entre 1900 e o ano corrente + 1 | Ano-modelo pode ser o próximo ano | n |
| Remoção | Cliente com veículos não pode ser removido (409); veículo com OS não pode ser removido (409, regra ativada no escopo `abertura-os-orcamento`) | Preserva histórico | n |
| Saída da suspensão | Reativação manual pelo funcionário | Decisão do usuário | y |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Cadastro de cliente ⭐ MVP

**User Story**: Como funcionário, quero cadastrar e manter clientes para identificá-los na abertura da OS.

**Why P1**: CRUD obrigatório e pré-requisito da OS.

**Acceptance Criteria**:

1. WHEN o funcionário cadastra um cliente com CPF válido (11 dígitos) ou CNPJ válido (14 dígitos) THEN o sistema SHALL persistir o cliente com situação ATIVO e responder 201.
2. IF os dígitos verificadores do CPF ou do CNPJ não conferem, ou todos os dígitos são iguais THEN o sistema SHALL responder 422 sem persistir.
3. IF já existe cliente com o mesmo CPF/CNPJ THEN o sistema SHALL responder 409.
4. WHEN o funcionário atualiza nome, e-mail ou telefone de um cliente THEN o sistema SHALL persistir os novos valores e manter o CPF/CNPJ.
5. WHEN o funcionário lista clientes THEN o sistema SHALL retornar a lista paginada.
6. IF o funcionário remove um cliente que possui veículos THEN o sistema SHALL responder 409.

**Independent Test**: Cadastrar CPF válido (201), inválido (422) e repetido (409).

---

### P1: Identificação do cliente ⭐ MVP

**User Story**: Como funcionário, quero localizar o cliente pelo CPF/CNPJ.

**Why P1**: Passo 3 do fluxo-os 3.1.

**Acceptance Criteria**:

1. WHEN o funcionário busca por um CPF/CNPJ cadastrado, com ou sem máscara THEN o sistema SHALL retornar o cliente com sua situação.
2. IF o CPF/CNPJ não está cadastrado THEN o sistema SHALL responder 404.

**Independent Test**: Buscar CPF cadastrado com máscara e sem máscara.

---

### P1: Cadastro de veículo ⭐ MVP

**User Story**: Como funcionário, quero cadastrar o veículo do cliente com placa, marca, modelo e ano.

**Why P1**: Funcionalidade obrigatória da criação da OS.

**Acceptance Criteria**:

1. WHEN o funcionário cadastra um veículo para um cliente existente com placa no padrão antigo (ABC-1234) ou Mercosul (ABC1D23) THEN o sistema SHALL persistir a placa normalizada e responder 201.
2. IF a placa não segue nenhum dos dois padrões THEN o sistema SHALL responder 422.
3. IF já existe veículo com a mesma placa normalizada THEN o sistema SHALL responder 409.
4. IF o ano está fora do intervalo 1900 até ano corrente + 1 THEN o sistema SHALL responder 400.
5. WHEN o funcionário busca veículo por placa THEN o sistema SHALL retornar o veículo e o cliente proprietário.
6. WHEN o funcionário lista os veículos de um cliente THEN o sistema SHALL retornar somente os veículos daquele cliente.

**Independent Test**: Cadastrar `abc-1234` e buscar por `ABC1234`.

---

### P1: Suspensão e reativação ⭐ MVP

**User Story**: Como funcionário, quero ver e reativar clientes suspensos por inadimplência.

**Why P1**: A abertura da OS rejeita clientes suspensos (fluxo-os 3.1) e a inadimplência suspende (5.2).

**Acceptance Criteria**:

1. WHEN o domínio suspende um cliente ATIVO THEN o sistema SHALL mudar a situação para SUSPENSO.
2. WHEN o funcionário reativa um cliente SUSPENSO THEN o sistema SHALL mudar a situação para ATIVO.
3. IF a reativação é pedida para cliente já ATIVO THEN o sistema SHALL responder 422.

**Independent Test**: Teste de domínio de `Cliente.suspender()` e `Cliente.reativar()`; integração da reativação.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| CLI-01 | P1: Cadastro de cliente | - | Pending |
| CLI-02 | P1: Identificação do cliente | - | Pending |
| CLI-03 | P1: Cadastro de veículo | - | Pending |
| CLI-04 | P1: Suspensão e reativação | - | Pending |

**Coverage:** 4 total, 0 mapped tasks, 4 unmapped ⚠️

---

## Success Criteria

- [ ] `CpfCnpj` e `Placa` com testes cobrindo casos válidos, dígitos inválidos, dígitos repetidos e formatos com máscara.
