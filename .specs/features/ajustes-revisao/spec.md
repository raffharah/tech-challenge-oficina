# Ajustes da revisão Specification

## Problem Statement

A revisão do grupo sobre a entrega da fase 1 encontrou seis pontos de atenção:

1. O Swagger não tem descrição nem exemplos de uso.
2. Listar clientes no Swagger exige um CPF/CNPJ.
3. Listar veículos no Swagger exige uma placa.
4. O cliente não tem como listar as próprias OS.
5. A documentação DDD não explicita entidades e objetos de valor.
6. Os status Recebida e Em diagnóstico acontecem no mesmo instante.

## Goals

- [x] REV-01: toda operação da API documentada com resumo, descrição, erros e exemplos.
- [x] REV-02: listagem de clientes sem filtro obrigatório.
- [x] REV-03: listagem geral de veículos, sem filtro obrigatório.
- [x] REV-04: cliente lista as próprias OS.
- [x] REV-05: agregados, entidades e objetos de valor documentados e ligados ao código.
- [x] REV-06: Recebida e Em diagnóstico como etapas distintas da OS.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Autenticação do cliente | AD-001; a listagem segue o mesmo modelo da consulta |
| Exemplos de resposta por DTO | DTOs ficam em `application`, sem dependência de Swagger; os esquemas já descrevem os campos |
| Atualizar o board do Figma e os PNG do Event Storming | Feito pelo grupo na ferramenta; o Markdown já reflete o fluxo |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Busca e listagem (REV-02, REV-03) | Uma operação paginada com filtro opcional; filtro sem resultado → 200 com página vazia | O OpenAPI não admite duas operações no mesmo caminho e método (AD-006) | y |
| Saída de Recebida (REV-06) | Comando explícito do funcionário `POST /ordens-servico/{id}/diagnostico` | AD-007 | y |
| Acesso à listagem do cliente (REV-04) | Sem token, só com o CPF/CNPJ do titular; sem OS → página vazia | AD-001 revisada em 2026-11-02 | y |
| Fronteiras de agregado (REV-05) | Veículo e Serviço são agregados próprios | Repositório e ciclo de vida independentes; OS e Cliente os referenciam só por id ou cópia | y |

**Open questions:** none.

---

## User Stories

### REV-01: Documentação da API

**Acceptance Criteria**:

1. WHEN o OpenAPI é gerado THEN toda operação SHALL ter tag, `summary` e `description`, e toda tag SHALL ter descrição.
2. WHEN o OpenAPI é gerado THEN todo campo de `*Request` e todo parâmetro SHALL ter descrição e exemplo.
3. WHEN uma operação declara erro 4xx THEN a resposta SHALL citar o código de negócio e usar o esquema `ErroResponse`; rotas com JWT SHALL declarar 401.

### REV-02: Listagem de clientes

1. WHEN `GET /clientes` é chamado sem parâmetros THEN o sistema SHALL listar todos os clientes paginados.
2. WHEN `cpfCnpj` é informado, com ou sem máscara THEN o sistema SHALL devolver a página só com o cliente daquele documento.
3. IF o documento não está cadastrado THEN o sistema SHALL responder 200 com página vazia; IF é inválido THEN SHALL responder 422.

### REV-03: Listagem de veículos

1. WHEN `GET /veiculos` é chamado sem parâmetros THEN o sistema SHALL listar todos os veículos paginados, por ordem de cadastro.
2. WHEN `placa` é informada, com hífen ou minúsculas THEN o sistema SHALL devolver a página só com aquele veículo.
3. IF a placa não está cadastrada THEN o sistema SHALL responder 200 com página vazia; IF é inválida THEN SHALL responder 422.

### REV-04: OS do cliente

1. WHEN o cliente chama `GET /cliente/ordens-servico?cpfCnpj=` sem token THEN o sistema SHALL devolver só as OS do titular, paginadas da mais nova para a mais antiga, com status e total.
2. IF o titular não tem OS THEN o sistema SHALL responder 200 com página vazia.
3. IF o CPF/CNPJ é inválido THEN o sistema SHALL responder 422; IF está ausente THEN SHALL responder 400.

### REV-05: Modelo de domínio

1. WHEN alguém lê a documentação DDD THEN ela SHALL listar, por contexto, os agregados, as entidades e os objetos de valor, com a regra de cada um e a classe correspondente.
2. As Decisões do MVP da linguagem ubíqua SHALL corresponder ao escopo implementado.

### REV-06: Diagnóstico explícito

1. WHEN o funcionário abre a OS THEN ela SHALL ficar `RECEBIDA`, com um único registro no histórico.
2. WHEN o funcionário inicia o diagnóstico THEN a OS SHALL ir para `EM_DIAGNOSTICO`, registrando data e hora próprias.
3. IF a OS não está `RECEBIDA` THEN iniciar o diagnóstico SHALL responder 422 `TRANSICAO_INVALIDA`.
4. WHILE a OS está `RECEBIDA`, incluir serviço ou item SHALL responder 422 `ORDEM_FORA_DE_DIAGNOSTICO`.
