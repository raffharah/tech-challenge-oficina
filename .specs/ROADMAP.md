# ROADMAP

Escopo do MVP = funcionalidades e requisitos técnicos obrigatórios de `docs/fase1-tech-challenge.md` (seções 5 e 6). O Event Storming (`docs/ddd/event-storming/`) dá nomes, comandos e regras; trilhas que não atendem a um item obrigatório ficam fora (AD-003).

Cada escopo é uma feature em `.specs/features/<escopo>/spec.md`. Design e tasks são criados quando o escopo começa. Decisões transversais em `.specs/STATE.md`.

## Escopos

| # | Escopo | Requisito obrigatório atendido | Depende de | Tamanho |
|---|---|---|---|---|
| 1 | [fundacao-plataforma](features/fundacao-plataforma/spec.md) | JWT admin, Swagger, execução local, cobertura 80%, camadas | - | Medium |
| 2 | [clientes-veiculos](features/clientes-veiculos/spec.md) | CRUD clientes e veículos, identificação por CPF/CNPJ, validação CPF/CNPJ e placa | 1 | Medium |
| 3 | [catalogo-servicos](features/catalogo-servicos/spec.md) | CRUD de serviços | 1 | Small |
| 4 | [estoque-itens](features/estoque-itens/spec.md) | CRUD de peças e insumos, controle de estoque | 1 | Medium |
| 5 | [abertura-os-orcamento](features/abertura-os-orcamento/spec.md) | Criação da OS, inclusão de serviços/peças/insumos, orçamento automático, disponibilização para aprovação, regras de transição | 2, 3, 4 | Large |
| 6 | [decisao-orcamento](features/decisao-orcamento/spec.md) | Registro da aprovação ou rejeição do orçamento | 5 | Medium |
| 7 | [execucao-servicos](features/execucao-servicos/spec.md) | Status Em execução → Finalizada → Entregue | 6 | Small |
| 8 | [acompanhamento-os](features/acompanhamento-os/spec.md) | Consulta do cliente via API, listagem, detalhamento, tempo médio de execução | 5 | Medium |

Ordem: 1 → (2, 3, 4) → 5 → 6 → 7; o 8 começa após o 5.

## Matriz de cobertura dos obrigatórios

| Item obrigatório (fase1-tech-challenge §5/§6) | Requisito |
|---|---|
| Identificação do cliente por CPF ou CNPJ | CLI-02, OS-01 |
| Cadastro do veículo com placa, marca, modelo e ano | CLI-03 |
| Inclusão dos serviços solicitados | OS-02 |
| Inclusão de peças e insumos | OS-02 |
| Geração automática do orçamento | OS-03 |
| Envio ou disponibilização do orçamento para aprovação | OS-03, ACO-01 |
| Registro da aprovação ou rejeição do orçamento | ORC-01, ORC-02 |
| Seis status, controle de alterações, regras de transição | OS-04, EXE-01, EXE-02 |
| Consulta do andamento pelo cliente via API | ACO-01 |
| CRUD de clientes / veículos / serviços / peças e insumos | CLI-01, CLI-03, SRV-01, EST-01 |
| Controle de estoque | EST-02, EST-03, ORC-01 |
| Listagem e detalhamento de OS | ACO-02 |
| Tempo médio de execução dos serviços | ACO-03 |
| JWT para APIs administrativas | FUND-02 |
| Validação de CPF/CNPJ e placa | CLI-01, CLI-03 |
| Swagger/OpenAPI | FUND-05 |
| Testes unitários, integração, cobertura 80% | FUND-04 |
| Execução local (Dockerfile, docker-compose) | FUND-01 |
| Transações nos fluxos principais | AD-002 |

## Fora do MVP (trilhas do Event Storming sem item obrigatório)

| Trilha | Origem |
|---|---|
| Notificações por e-mail (orçamento, execução, finalização, funcionário) | fluxo-os 3.2, 4.1, 5.1; orçamento é disponibilizado pela consulta do cliente |
| Recusa parcial e renegociação | fluxo-os 3.3 |
| Serviço adicional e histórico de orçamentos | fluxo-os 4.2 |
| Prazo e atraso da OS | fluxo-os 4.1 |
| Início/conclusão por serviço | fluxo-os 4.1; tempo médio usa o histórico de status (AD-005) |
| Cobrança, pagamento, nota fiscal, notificação de retirada | fluxo-os 5.1 |
| Inadimplência, suspensão do cliente, devolução de peças | fluxo-os 5.2; estoque 7.2 |
| Reserva e saldo reservado | estoque 6.3; baixa direta na aprovação (AD-004) |
| Estoque mínimo, descarte, inativação de item | estoque 5.3, 7.1 |
| Fornecedor, item fora de linha, peça defeituosa, garantia | estoque 5.2, 6.2, 7.3 |

## Conflitos com a documentação existente

O corte diverge de documentos que hoje se declaram fonte de verdade. Atualizar antes de implementar:

- `docs/ddd/linguagem-ubiqua.md`, Decisões do MVP: linhas Reserva, Liberação da reserva, Estoque mínimo, Prazo, Agregados do Estoque (Reserva), Agregados da OS (Pagamento, Nota Fiscal) e Sistemas externos (e-mail, pagamento, nota fiscal).
- `AGENTS.md`: árvore de exemplo cita `cobranca/`, `Reserva`, `ReservarItens`, `SistemaDePagamentos`; tabela de contextos inclui `cobranca`.
- `docs/ddd/c4/`: fachadas de pagamento, nota fiscal, fornecedor e componente de notificações.

## Entregáveis fora das specs

- [ ] README.md com os itens mínimos de `docs/fase1-tech-challenge.md`.
- [ ] Relatório de análise de vulnerabilidades.
- [ ] Event Storming e diagramas exportados em `docs/ddd/`.
- [ ] Evidência de cobertura ≥ 80% (`target/site/jacoco/index.html`).
- [ ] PDF de entrega com branch e hash do commit final.
