# Clientes e Veículos Specification

## Problem Statement

A oficina perde o histórico de clientes e veículos. O MVP exige CRUD de clientes e veículos, identificação do cliente por CPF/CNPJ, cadastro do veículo com placa, marca, modelo e ano, e validação de CPF/CNPJ e placa.

## Goals

- [ ] CRUD de clientes e veículos com CPF/CNPJ e placa validados no objeto de valor.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Suspensão por inadimplência | AD-003: inadimplência fora do MVP |
| Consulta à Receita Federal | Validação por formato e dígitos verificadores |
| Alterar CPF/CNPJ ou dono do veículo | Sem item obrigatório |
| E-mail e telefone do cliente | Sem notificações no MVP (AD-003) |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Dados do cliente | Nome e CPF/CNPJ | Mínimo para identificar | n |
| Normalização | CPF/CNPJ só dígitos; placa maiúscula sem hífen; entrada aceita máscara | Unicidade independe da formatação | n |
| Ano do veículo | 1900 até ano corrente + 1 | Ano-modelo pode ser o próximo | n |
| Remoção | Cliente com veículos e veículo com OS não são removidos (409) | Preserva histórico | n |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: CRUD de clientes ⭐ MVP

**User Story**: Como funcionário, quero cadastrar e manter clientes.

**Why P1**: CRUD e validação de CPF/CNPJ obrigatórios.

**Acceptance Criteria**:

1. WHEN o funcionário cadastra cliente com CPF válido (11 dígitos) ou CNPJ válido (14 dígitos) THEN o sistema SHALL persistir e responder 201.
2. IF os dígitos verificadores não conferem ou todos os dígitos são iguais THEN o sistema SHALL responder 422 sem persistir.
3. IF já existe cliente com o mesmo CPF/CNPJ THEN o sistema SHALL responder 409.
4. WHEN o funcionário atualiza o nome THEN o sistema SHALL persistir mantendo o CPF/CNPJ.
5. WHEN o funcionário lista clientes THEN o sistema SHALL retornar lista paginada.
6. IF o funcionário remove cliente que possui veículos THEN o sistema SHALL responder 409.

**Independent Test**: CPF válido (201), inválido (422), repetido (409).

---

### P1: Identificação do cliente ⭐ MVP

**User Story**: Como funcionário, quero localizar o cliente pelo CPF/CNPJ.

**Why P1**: Item obrigatório da criação da OS.

**Acceptance Criteria**:

1. WHEN o funcionário busca CPF/CNPJ cadastrado, com ou sem máscara THEN o sistema SHALL retornar o cliente.
2. IF o CPF/CNPJ não está cadastrado THEN o sistema SHALL responder 404.

**Independent Test**: Buscar o mesmo CPF com e sem máscara.

---

### P1: CRUD de veículos ⭐ MVP

**User Story**: Como funcionário, quero cadastrar o veículo do cliente com placa, marca, modelo e ano.

**Why P1**: CRUD e validação de placa obrigatórios.

**Acceptance Criteria**:

1. WHEN o funcionário cadastra veículo para cliente existente com placa no padrão antigo (ABC-1234) ou Mercosul (ABC1D23) THEN o sistema SHALL persistir a placa normalizada e responder 201.
2. IF a placa não segue nenhum dos dois padrões THEN o sistema SHALL responder 422.
3. IF já existe veículo com a mesma placa normalizada THEN o sistema SHALL responder 409.
4. IF o ano está fora de 1900 até ano corrente + 1 THEN o sistema SHALL responder 422.
5. WHEN o funcionário atualiza marca, modelo ou ano THEN o sistema SHALL persistir mantendo placa e cliente.
6. WHEN o funcionário lista os veículos de um cliente THEN o sistema SHALL retornar só os veículos dele.
7. IF o funcionário remove veículo referenciado por OS THEN o sistema SHALL responder 409.

**Independent Test**: Cadastrar `abc-1234` e buscar por `ABC1234`.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| CLI-01 | P1: CRUD de clientes | Tasks | In Tasks |
| CLI-02 | P1: Identificação do cliente | Tasks | In Tasks |
| CLI-03 | P1: CRUD de veículos | Tasks | In Tasks |

**Coverage:** 3 total, 3 mapped tasks, 0 unmapped

---

## Success Criteria

- [ ] `CpfCnpj` e `Placa` testados com casos válidos, dígitos inválidos, dígitos repetidos e máscara.
