# Fluxo de Ordem de Serviço (OS): descrição completa do Event Storming

Os diagramas estão em Mermaid e descem de cima para baixo; as setas tracejadas são os desvios. As tabelas repetem o mesmo conteúdo em texto, passo a passo.

Nos diagramas Mermaid deste documento: caixa **azul** = comando (CMD), **laranja** = evento (EV), **roxa** = política (PO), **rosa** = sistema externo (SE). Linha **contínua** = caminho principal; linha **tracejada** = exceção. Atores e modelos de leitura não aparecem nos diagramas e estão nas tabelas.

## 1. Atores, modelos de leitura e sistemas externos

**Atores:** Cliente (solicita o serviço, aprova ou recusa o orçamento, paga) e Funcionário (identifica e cadastra cliente e veículo, cria e atualiza a OS, executa o serviço, entrega o veículo).

**Sistemas externos (SE):**

| Sistema | Onde é usado |
|---|---|
| Receita Federal | Regra de validação de CPF/CNPJ (aparece só no texto de uma política) |
| Sistema de estoque de itens | Consulta ao adicionar peças e insumos à OS |
| Sistema de gestão de peças e insumos | Devolução de peças ao estoque no cancelamento por inadimplência |
| Sistema de envio de e-mail (Provedor) | Todas as notificações ao cliente |
| Sistema de gestão de pagamentos | Cobrança e pagamento |
| Sistema de nota fiscal eletrônica | Emissão da nota fiscal |

**Modelos de leitura (ML) usados:** Lista de serviços disponíveis oferecidos pela oficina; Lista de clientes cadastrados; Lista de veículos cadastrados; Lista de serviços com peças e insumos necessários; Lista dos componentes do orçamento da OS; Lista dos serviços da OS; Lista dos serviços disponíveis; Detalhamento do orçamento e instruções para pagamento.

## 2. Ciclo de vida da OS

```mermaid
flowchart LR
  r[recebida] --> d[em diagnóstico] --> a[aguardando aprovação] --> e[em execução] --> f[finalizada] --> g[entregue]
  a -.recusa parcial.-> d
  e -.problema no veículo.-> d
  a -.orçamento rejeitado.-> c[cancelada]
  f -.pagamento vencido.-> c
```

A OS nasce "recebida" e passa logo para "em diagnóstico". O orçamento a leva para "aguardando aprovação". Aprovado, ela vai para "em execução", depois "finalizada" e, após pagamento e retirada, "entregue". Ela volta a "em diagnóstico" em duas situações (recusa parcial do orçamento e problema encontrado no veículo durante o serviço) e termina "cancelada" quando o orçamento é rejeitado ou o pagamento vence.

## 3. Fase 1: Criação da OS

A fase vai do pedido do cliente até a aprovação do orçamento e termina no primeiro Ponto Pivotal. Ela tem três partes: identificação do cliente e do veículo, diagnóstico e orçamento, e o tratamento da recusa do orçamento.

### 3.1 Identificação do cliente e do veículo

```mermaid
flowchart TD
  classDef cmd fill:#bfe0ff,stroke:#2b78c2,color:#000
  classDef ev fill:#ffd9a0,stroke:#c77d00,color:#000
  classDef po fill:#dcc8ff,stroke:#7b4fd1,color:#000
  A1["CMD Solicita um serviço"]:::cmd --> A2["EV Pedido de serviço solicitado"]:::ev
  A2 --> A3["PO Validar CPF/CNPJ pelas regras da Receita Federal"]:::po
  A3 --> A4["CMD Valida CPF/CNPJ"]:::cmd --> A5["EV CPF/CNPJ validado"]:::ev
  A5 --> A6["CMD Identifica cliente"]:::cmd --> A7["EV Cliente identificado"]:::ev
  A5 -.->|cliente não existe| A8["CMD Cadastra cliente"]:::cmd -.-> A9["EV Cliente cadastrado"]:::ev
  A7 -.->|verifica suspensão| A10["PO Se o cliente estiver suspenso, rejeitar o serviço"]:::po -.-> A11["CMD Identifica se o cliente está suspenso"]:::cmd -.-> A12["EV Pedido de serviço rejeitado por suspensão do cliente"]:::ev
  A7 --> A13["PO Validar formato da placa; se inválida, rejeitar"]:::po
  A9 -.-> A13
  A13 --> A14["CMD Valida placa"]:::cmd --> A15["EV Placa do veículo validada"]:::ev
  A15 --> A16["CMD Identifica veículo"]:::cmd --> A17["EV Veículo identificado"]:::ev
  A15 -.->|veículo não existe| A18["CMD Cadastra veículo"]:::cmd -.-> A19["EV Veículo cadastrado"]:::ev
  A17 --> FIM(["segue para a criação da OS"])
  A19 -.-> FIM
```

| # | Ator e modelo de leitura | Comando | Evento | Política associada |
|---|---|---|---|---|
| 1 | Cliente. ML: Lista de serviços disponíveis oferecidos pela oficina | Solicita um serviço | Pedido de serviço solicitado | Validar o CPF/CNPJ de acordo com as regras da Receita Federal (fica entre este evento e o próximo comando) |
| 2 | n/a | Valida CPF/CNPJ | CPF/CNPJ validado | n/a |
| 3 | Funcionário. ML: Lista de clientes cadastrados | Identifica cliente | Cliente identificado | n/a |
| 4 | n/a | Valida placa | Placa do veículo validada | Validar o formato da placa; se inválida, rejeitar (antecede o comando; recebe setas de "Cliente identificado" e de "Cliente cadastrado") |
| 5 | Funcionário. ML: Lista de veículos cadastrados | Identifica veículo | Veículo identificado | n/a |

**Desvios desta parte (tracejados):**

| Desvio | Sai de | Sequência | Para onde vai |
|---|---|---|---|
| Cliente não cadastrado | CPF/CNPJ validado | Funcionário (ML Lista de clientes cadastrados): Cadastra cliente, evento Cliente cadastrado | Política de validação da placa (passo 4) |
| Cliente suspenso | Cliente identificado | Política "Identificar se o cliente está suspenso, caso esteja rejeitar o serviço", comando Identifica se o cliente está suspenso, evento Pedido de serviço rejeitado por suspensão do cliente | Fim (o pedido é rejeitado) |
| Veículo não cadastrado | Placa do veículo validada | Funcionário (ML Lista de veículos cadastrados): Cadastra veículo, evento Veículo cadastrado | Comando Cria Ordem de Serviço (passo 6) |

### 3.2 Diagnóstico e orçamento até a aprovação

```mermaid
flowchart TD
  classDef cmd fill:#bfe0ff,stroke:#2b78c2,color:#000
  classDef ev fill:#ffd9a0,stroke:#c77d00,color:#000
  classDef po fill:#dcc8ff,stroke:#7b4fd1,color:#000
  classDef se fill:#ffb6dc,stroke:#c2417c,color:#000
  A20["CMD Cria Ordem de Serviço"]:::cmd --> A21["EV OS criada com status recebida"]:::ev --> A22["PO Sistema atualiza a OS para em diagnóstico"]:::po --> A23["CMD Atualiza OS para em diagnóstico"]:::cmd --> A24["EV OS atualizada para em diagnóstico"]:::ev
  A24 --> A25["CMD Adiciona serviços, peças e insumos à OS"]:::cmd --> A27["EV Serviços, peças e insumos adicionados à OS"]:::ev
  A25 --- A26["SE Sistema de estoque de itens"]:::se
  A27 --> A28["PO Somatório simples do valor padrão dos serviços e dos valores de peças e insumos recuperados do contexto de estoque"]:::po --> A29["CMD Calcula orçamento do serviço"]:::cmd --> A30["EV Orçamento calculado"]:::ev
  A30 --> A31["PO Notificar contendo os detalhes do orçamento"]:::po --> A32["CMD Envia orçamento para o cliente"]:::cmd --> A34["EV Orçamento enviado ao cliente"]:::ev
  A32 --- A33["SE Sistema de envio de e-mail"]:::se
  A34 --> A35["PO Sistema atualiza a OS para aguardando aprovação"]:::po --> A36["CMD Atualiza OS para aguardando aprovação"]:::cmd --> A37["EV OS atualizada para aguardando aprovação"]:::ev
  A37 --> A38["CMD Aprova o orçamento"]:::cmd --> A39["EV Orçamento aprovado"]:::ev --> A40["PO Notificar o funcionário sobre a aprovação"]:::po --> A41["CMD Notifica funcionário sobre a aprovação"]:::cmd --> A42["EV Funcionário notificado sobre aprovação"]:::ev
  A42 --> PIV(["Ponto Pivotal: segue para Serviço"])
```

| # | Ator e modelo de leitura | Comando | Evento | Política associada | Sistema externo |
|---|---|---|---|---|---|
| 6 | Funcionário | Cria Ordem de Serviço | Ordem de serviço criada com status "recebida" | Depois do evento: O Sistema atualiza a OS para "em diagnóstico" | n/a |
| 7 | n/a | Atualiza OS para "em diagnóstico" | Ordem de serviço atualizada para o status "em diagnóstico" | n/a | n/a |
| 8 | Funcionário. ML: Lista de serviços com peças e insumos necessários | Adiciona serviços, peças e insumos à OS | Serviços, peças e insumos adicionados à OS | n/a | Sistema de estoque de itens |
| 9 | n/a | Calcula orçamento do serviço | Orçamento calculado | Antes do comando: somatório simples do valor padrão dos serviços e dos valores de peças e insumos recuperados do contexto de estoque | n/a |
| 10 | n/a | Envia orçamento para o cliente | Orçamento enviado ao cliente | Antes: notificar contendo os detalhes do orçamento. Depois: O Sistema atualiza a OS para "aguardando aprovação" | Sistema de envio de e-mail |
| 11 | n/a | Atualiza OS para "aguardando aprovação" | OS atualizada para o status "aguardando aprovação" | n/a | n/a |
| 12 | Cliente. ML: Lista dos componentes do orçamento da OS | Aprova o orçamento | Orçamento aprovado | Notificar o funcionário sobre a aprovação do orçamento | n/a |
| 13 | n/a | Notifica funcionário sobre a aprovação do orçamento | Funcionário notificado sobre aprovação do orçamento | n/a | n/a |

Depois do passo 13, uma linha contínua atravessa o primeiro Ponto Pivotal e inicia a fase Serviço.

### 3.3 Exceção: recusa do orçamento

Depois de **"OS atualizada para aguardando aprovação"** (passo 11), o cliente tem três saídas: aprovar (passo 12, linha contínua), **recusar parcialmente** os componentes ou **recusar totalmente** (as duas, tracejadas). A recusa parcial reabre o diagnóstico, recalcula o orçamento e o envia de novo; **nesse segundo envio o cliente só pode aprovar ou rejeitar**, de modo que a renegociação acontece uma única vez.

```mermaid
flowchart TD
  classDef cmd fill:#bfe0ff,stroke:#2b78c2,color:#000
  classDef ev fill:#ffd9a0,stroke:#c77d00,color:#000
  classDef po fill:#dcc8ff,stroke:#7b4fd1,color:#000
  classDef se fill:#ffb6dc,stroke:#c2417c,color:#000
  S(["OS atualizada para aguardando aprovação"]) -.->|recusa parcial| B1["CMD Recusa parcial de componentes do orçamento"]:::cmd -.-> B2["EV Componentes do orçamento recusados"]:::ev -.-> B3["PO Estado do orçamento é alterado para rejeitado"]:::po -.-> B4["CMD Rejeita o orçamento inicial"]:::cmd -.-> B5["EV Orçamento inicial rejeitado"]:::ev
  B5 -.-> B6["PO Notificar funcionário sobre a rejeição"]:::po -.-> B7["CMD Notifica funcionário sobre a rejeição"]:::cmd -.-> B8["EV Funcionário notificado sobre rejeição"]:::ev
  B8 -.-> B9["CMD Reorganiza a OS"]:::cmd -.-> B10["EV OS reorganizada"]:::ev -.-> B11["PO Sistema atualiza a OS para em diagnóstico"]:::po -.-> B12["CMD Atualiza OS para em diagnóstico"]:::cmd -.-> B13["EV OS atualizada para em diagnóstico"]:::ev
  B13 -.-> B14["PO Sistema recalcula o orçamento com as novas condições"]:::po -.-> B15["CMD Recalcula o orçamento"]:::cmd -.-> B16["EV Orçamento recalculado"]:::ev
  B16 -.-> B17["PO Notificar contendo os detalhes do orçamento"]:::po -.-> B18["CMD Envia orçamento para o cliente"]:::cmd -.-> B20["EV Orçamento atualizado enviado ao cliente"]:::ev
  B18 --- B19["SE Sistema de envio de e-mail"]:::se
  B20 -.-> B21["PO Sistema atualiza a OS para aguardando aprovação"]:::po -.-> B22["CMD Atualiza OS para aguardando aprovação"]:::cmd -.-> B23["EV OS atualizada para aguardando aprovação"]:::ev
  B23 -.->|cliente aprova| APR(["volta ao comando Aprova o orçamento (passo 12)"])
  B23 -.->|cliente rejeita| B24["CMD Rejeita o orçamento"]:::cmd -.-> B25["EV Orçamento Rejeitado"]:::ev
```

```mermaid
flowchart TD
  classDef cmd fill:#bfe0ff,stroke:#2b78c2,color:#000
  classDef ev fill:#ffd9a0,stroke:#c77d00,color:#000
  classDef po fill:#dcc8ff,stroke:#7b4fd1,color:#000
  S(["OS atualizada para aguardando aprovação"]) -.->|recusa total| C1["CMD Recusa total dos componentes do orçamento"]:::cmd -.-> B25["EV Orçamento Rejeitado"]:::ev
  R(["Rejeita o orçamento, depois do recálculo"]) -.-> B25
  B25 -.-> B26["PO Sistema atualiza a OS para cancelada"]:::po -.-> B27["CMD Atualiza OS para cancelada"]:::cmd -.-> B28["EV OS atualizada com status cancelada"]:::ev
  B28 -.-> B29["PO Notificar funcionário sobre a rejeição do orçamento"]:::po -.-> B30["CMD Notifica funcionário sobre a rejeição do orçamento"]:::cmd -.-> B31["EV Funcionário notificado sobre rejeição do orçamento"]:::ev
  B31 -.-> FIM(["fim: OS cancelada"])
```

**Recusa parcial (volta ao diagnóstico):**

| # | Ator e modelo de leitura | Comando | Evento | Política associada |
|---|---|---|---|---|
| R1 | Cliente. ML: Lista dos componentes do orçamento da OS | Recusa parcial de componentes do orçamento | Componentes do orçamento recusados | Estado do orçamento é alterado para rejeitado |
| R2 | n/a | Rejeita o orçamento inicial | Orçamento inicial rejeitado | Notificar o funcionário sobre a rejeição do orçamento |
| R3 | n/a | Notifica funcionário sobre a rejeição do orçamento | Funcionário notificado sobre rejeição do orçamento | n/a |
| R4 | Funcionário. ML: Lista dos componentes do orçamento da OS | Reorganiza a OS | OS reorganizada | O Sistema atualiza a OS para "em diagnóstico" |
| R5 | n/a | Atualiza OS para "em diagnóstico" | Ordem de serviço atualizada para o status "em diagnóstico" | O Sistema recalcula o orçamento da OS baseado nas novas condições |
| R6 | n/a | Recalcula o orçamento | Orçamento recalculado | Notificar contendo os detalhes do orçamento |
| R7 | n/a | Envia orçamento para o cliente (SE e-mail) | Orçamento atualizado enviado ao cliente | O Sistema atualiza a OS para "aguardando aprovação" |
| R8 | n/a | Atualiza OS para "aguardando aprovação" | OS atualizada para o status "aguardando aprovação" | n/a |

Do evento R8 o cliente pode **aprovar** (seta tracejada de volta ao comando "Aprova o orçamento", e a partir daí o fluxo principal segue) ou **rejeitar**.

**Rejeição e cancelamento:**

| # | Ator e modelo de leitura | Comando | Evento | Política associada |
|---|---|---|---|---|
| J1a | Cliente. ML: Lista dos componentes do orçamento da OS | Recusa total dos componentes do orçamento (vem do passo 11) | Orçamento Rejeitado | O Sistema atualiza a OS para "cancelada" |
| J1b | Cliente. ML: Lista dos componentes do orçamento da OS | Rejeita o orçamento (vem do evento R8) | Orçamento Rejeitado | (mesmo evento de J1a) |
| J2 | n/a | Atualiza OS para "cancelada" | OS atualizada com status "cancelada" | Notificar o funcionário sobre a rejeição do orçamento |
| J3 | n/a | Notifica funcionário sobre a rejeição do orçamento | Funcionário notificado sobre rejeição do orçamento | Fim do fluxo |

## 4. Fase 2: Serviço

A fase começa logo depois do primeiro Ponto Pivotal, com a OS passando para "em execução", e termina quando a OS é finalizada e o cliente é avisado.

### 4.1 Caminho principal e serviço atrasado

```mermaid
flowchart TD
  classDef cmd fill:#bfe0ff,stroke:#2b78c2,color:#000
  classDef ev fill:#ffd9a0,stroke:#c77d00,color:#000
  classDef po fill:#dcc8ff,stroke:#7b4fd1,color:#000
  classDef se fill:#ffb6dc,stroke:#c2417c,color:#000
  S1["PO Sistema atualiza a OS para em execução"]:::po --> S2["CMD Atualiza OS para em execução"]:::cmd --> S3["EV OS atualizada para em execução"]:::ev
  S3 --> S4["PO Notificar o cliente que a OS está em execução"]:::po --> S5["CMD Notifica o cliente que a OS está em execução"]:::cmd --> S6["EV Cliente notificado que a OS está em execução"]:::ev
  S5 --- SE1["SE Sistema de envio de e-mail"]:::se
  S6 --> S7["CMD Inicia serviço"]:::cmd --> S8["EV Serviço iniciado"]:::ev
  S8 --> S9["CMD Finaliza serviço"]:::cmd --> S10["EV Serviço finalizado"]:::ev --> S11["PO Atualizar o estado da OS para finalizada"]:::po --> S12["CMD Atualiza OS para finalizada"]:::cmd --> S13["EV OS atualizada com status finalizada"]:::ev --> S14["PO Notificar o cliente que a OS está finalizada"]:::po
  S8 -.->|prazo do orçamento| T1["PO Se o prazo definido previamente no orçamento tiver sido ultrapassado, definir o serviço como atrasado"]:::po -.-> T2["CMD Atualiza serviço para em atraso"]:::cmd -.-> T3["EV Serviço atrasado"]:::ev
  T3 -.-> T4["PO Notificar informando que o serviço atrasou"]:::po -.-> T5["CMD Notifica o cliente que o serviço atrasou"]:::cmd -.-> T6["EV Cliente foi notificado com nova data"]:::ev -.-> S9
  T5 --- SE2["SE Sistema de envio de e-mail"]:::se
  S14 --> PIV(["Ponto Pivotal: segue para Pagamento e Finalização"])
```

| # | Ator e modelo de leitura | Comando | Evento | Política associada | Sistema externo |
|---|---|---|---|---|---|
| 1 | n/a | Atualiza OS para "em execução" | OS atualizada para o status "em execução" | Antes: O Sistema atualiza a OS para "em execução" (logo após o Ponto Pivotal). Depois: Notificar o cliente que a OS está em execução | n/a |
| 2 | n/a | Notifica o cliente que a OS está em execução | Cliente notificado que a OS está em execução | n/a | Sistema de envio de e-mail |
| 3 | Funcionário. ML: Lista dos serviços da OS | Inicia serviço | Serviço iniciado | n/a | n/a |
| 4 | Funcionário. ML: Lista dos serviços da OS | Finaliza serviço | Serviço finalizado | Atualizar o estado da OS para "finalizada" | n/a |
| 5 | n/a | Atualiza OS para "finalizada" | OS atualizada com status de "finalizada" | Notificar o cliente que a OS está finalizada | n/a |

**Desvio: serviço atrasado.** A partir de *Serviço iniciado* uma trilha tracejada mostra: política de prazo ("Se o prazo definido previamente no orçamento tiver sido ultrapassado definir o serviço como atrasado"), comando *Atualiza serviço para em atraso*, evento *Serviço atrasado*, política *Notificar informando que o serviço atrasou*, comando *Notifica o cliente que o serviço atrasou* (SE e-mail) e evento *Cliente foi notificado com nova data*. A trilha volta ao comando *Finaliza serviço* (passo 4).

### 4.2 Desvio: problema no veículo e serviço adicional

A partir de *Serviço iniciado* outra trilha tracejada trata o caso em que o funcionário encontra um problema no veículo. A OS volta ao diagnóstico, o orçamento é recalculado e o cliente decide sobre o **novo orçamento**: aceitar (o serviço é retomado) ou rejeitar (volta-se ao orçamento anterior e o serviço também é retomado).

```mermaid
flowchart TD
  classDef cmd fill:#bfe0ff,stroke:#2b78c2,color:#000
  classDef ev fill:#ffd9a0,stroke:#c77d00,color:#000
  classDef po fill:#dcc8ff,stroke:#7b4fd1,color:#000
  classDef se fill:#ffb6dc,stroke:#c2417c,color:#000
  I(["Serviço iniciado"]) -.-> V1["CMD Identifica problema no veículo"]:::cmd -.-> V2["EV Problema no veículo identificado"]:::ev -.-> V3["CMD Funcionário adiciona um serviço adicional"]:::cmd -.-> V4["EV Serviço adicional adicionado"]:::ev
  V4 -.-> V5["PO Sistema atualiza a OS para em diagnóstico"]:::po -.-> V6["CMD Atualiza a OS para em diagnóstico"]:::cmd -.-> V7["EV OS atualizada para em diagnóstico"]:::ev
  V7 -.-> V8["PO Sistema recalcula o orçamento com as novas condições"]:::po -.-> V9["CMD Recalcula o orçamento"]:::cmd -.-> V10["EV Orçamento recalculado"]:::ev
  V10 -.-> V11["PO Notificar contendo os detalhes do orçamento"]:::po -.-> V12["CMD Envia um novo orçamento ao cliente"]:::cmd -.-> V13["EV Novo orçamento enviado ao cliente"]:::ev
  V12 --- V12S["SE Sistema de envio de e-mail"]:::se
  V13 -.-> V14["PO Sistema atualiza a OS para aguardando aprovação"]:::po -.-> V15["CMD Atualiza OS para aguardando aprovação"]:::cmd -.-> V16["EV OS atualizada para aguardando aprovação"]:::ev
  V16 -.->|cliente aceita| V17["CMD Aceita o orçamento"]:::cmd -.-> V18["EV Novo orçamento aceito"]:::ev -.-> V19["PO Notificar o funcionário sobre o orçamento"]:::po
  V16 -.->|cliente rejeita| V20["CMD Rejeita o orçamento"]:::cmd -.-> V21["EV Novo orçamento rejeitado"]:::ev -.-> V22["PO Obtém o orçamento anterior do histórico para retomar o serviço acordado anteriormente"]:::po -.-> V23["CMD Retoma orçamento anterior"]:::cmd -.-> V24["EV Orçamento anterior retomado"]:::ev -.-> V19
  V19 -.-> V25["CMD Notifica funcionário sobre o orçamento"]:::cmd -.-> V26["EV Funcionário notificado sobre o orçamento"]:::ev -.-> V27["PO Sistema atualiza a OS para em execução"]:::po -.-> V28["CMD Atualiza OS para em execução"]:::cmd -.-> V29["EV OS atualizada para em execução"]:::ev
  V29 -.-> V30["CMD Retoma serviço"]:::cmd -.-> V31["EV Serviço retomado"]:::ev -.-> FIM(["volta ao comando Finaliza serviço"])
```

| # | Ator e modelo de leitura | Comando | Evento | Política associada |
|---|---|---|---|---|
| V1 | Funcionário | Identifica problema no veículo | Problema no veículo identificado | n/a |
| V2 | Funcionário. ML: Lista dos serviços disponíveis | Funcionário adiciona um serviço adicional | Serviço adicional adicionado | n/a |
| V3 | n/a | Atualiza a OS para o status "em diagnóstico" | OS atualizada para o status "em diagnóstico" | Antes: O Sistema atualiza a OS para "em diagnóstico". Depois: O Sistema recalcula o orçamento da OS baseado nas novas condições |
| V4 | n/a | Recalcula o orçamento | Orçamento recalculado | Notificar contendo os detalhes do orçamento |
| V5 | n/a | Envia um novo orçamento ao cliente (SE e-mail) | Novo orçamento enviado ao cliente | O Sistema atualiza a OS para "aguardando aprovação" |
| V6 | n/a | Atualiza OS para o status "aguardando aprovação" | OS atualizada para o status "aguardando aprovação" | n/a |
| V7a | Cliente. ML: Lista dos componentes do orçamento da OS | Aceita o orçamento | Novo orçamento aceito | Notificar o funcionário sobre o orçamento |
| V7b | Cliente. ML: Lista dos componentes do orçamento da OS | Rejeita o orçamento | Novo orçamento rejeitado | Obtém o orçamento anterior do histórico para retomar o serviço acordado anteriormente |
| V8b | n/a | Retoma orçamento anterior | Orçamento anterior retomado | Notificar o funcionário sobre o orçamento |
| V9 | n/a | Notifica funcionário sobre o orçamento | Funcionário notificado sobre o orçamento | O Sistema atualiza a OS para "em execução" |
| V10 | n/a | Atualiza OS para o status "em execução" | OS atualizada para o status "em execução" | n/a |
| V11 | Funcionário | Retoma serviço | Serviço retomado | Fim: volta ao comando Finaliza serviço |

Os ramos **aceita** e **rejeita** convergem na política "Notificar o funcionário sobre o orçamento" e seguem juntos a partir daí.

## 5. Fase 3: Pagamento e Finalização

A fase começa logo depois do segundo Ponto Pivotal (o cliente é notificado de que a OS está finalizada), cobra o cliente, emite a nota fiscal e entrega o veículo.

### 5.1 Caminho principal

```mermaid
flowchart TD
  classDef cmd fill:#bfe0ff,stroke:#2b78c2,color:#000
  classDef ev fill:#ffd9a0,stroke:#c77d00,color:#000
  classDef po fill:#dcc8ff,stroke:#7b4fd1,color:#000
  classDef se fill:#ffb6dc,stroke:#c2417c,color:#000
  P1["CMD Notifica o cliente que a OS está finalizada"]:::cmd --> P2["EV Cliente notificado da finalização da OS"]:::ev --> P3["PO Quando o cliente for notificado da finalização, enviar a cobrança"]:::po --> P4["CMD Notifica o cliente de cobrança pendente"]:::cmd --> P5["EV Cobrança enviada ao cliente"]:::ev
  P5 --> P6["CMD Efetua pagamento"]:::cmd --> P7["EV Pagamento efetuado"]:::ev --> P8["PO Quando o pagamento for efetuado, registrar na OS"]:::po --> P9["CMD Registra pagamento na OS"]:::cmd --> P10["EV Pagamento registrado na OS"]:::ev
  P10 --> P11["PO Emitir nota fiscal após pagamento"]:::po --> P12["CMD Emite Nota Fiscal"]:::cmd --> P13["EV Nota fiscal emitida"]:::ev --> P14["PO Notificar com a nota fiscal para o cliente"]:::po --> P15["CMD Envia Nota Fiscal ao cliente"]:::cmd --> P16["EV Nota fiscal enviada ao cliente"]:::ev
  P16 --> P17["PO Enviar notificação para retirada do veículo"]:::po --> P18["CMD Envia notificação de retirada"]:::cmd --> P19["EV Cliente notificado para a retirada do veículo"]:::ev
  P19 --> P20["CMD Entrega o veículo ao cliente"]:::cmd --> P21["EV Veículo entregue ao cliente"]:::ev --> P22["CMD Atualiza OS para entregue"]:::cmd --> P23["EV OS atualizada para entregue"]:::ev
  P20 -.-> P24["EV Veículo devolvido ao cliente"]:::ev
  P1 --- E1["SE E-mail"]:::se
  P4 --- E2["SE E-mail"]:::se
  P4 --- G1["SE Gestão de pagamentos"]:::se
  P6 --- G2["SE Gestão de pagamentos"]:::se
  P12 --- N1["SE Nota fiscal eletrônica"]:::se
  P15 --- E3["SE E-mail"]:::se
  P18 --- E4["SE E-mail"]:::se
```

| # | Ator e modelo de leitura | Comando | Evento | Política associada | Sistema externo |
|---|---|---|---|---|---|
| 1 | n/a | Notifica o cliente que a OS está finalizada | Cliente notificado da finalização da OS | Quando o cliente for notificado da finalização, enviar a cobrança | Sistema de envio de e-mail |
| 2 | n/a | Notifica o cliente de cobrança pendente | Cobrança enviada ao cliente | n/a | Sistema de envio de e-mail e Sistema de gestão de pagamentos |
| 3 | Cliente. ML: Detalhamento do orçamento e instruções para pagamento | Efetua pagamento | Pagamento efetuado | Quando o pagamento for efetuado, registrar na OS | Sistema de gestão de pagamentos |
| 4 | n/a | Registra pagamento na OS | Pagamento registrado na OS | Emitir nota fiscal após pagamento | n/a |
| 5 | n/a | Emite Nota Fiscal | Nota fiscal emitida | Notificar com a nota fiscal para o cliente | Sistema de nota fiscal eletrônica |
| 6 | n/a | Envia Nota Fiscal ao cliente | Nota fiscal enviada ao cliente | Enviar notificação para retirada do veículo | Sistema de envio de e-mail |
| 7 | n/a | Envia notificação de retirada | Cliente notificado para a retirada do veículo | n/a | Sistema de envio de e-mail |
| 8 | Funcionário | Entrega o veículo ao cliente | Veículo entregue ao cliente | n/a | n/a |
| 9 | Funcionário | Atualiza OS para o status "entregue" | OS atualizada para o status "entregue" | Fim do processo | n/a |

Do comando *Entrega o veículo ao cliente* sai um ramo tracejado para o evento *Veículo devolvido ao cliente*, sem continuação.

### 5.2 Desvio: pagamento em atraso, vencido e cancelamento

```mermaid
flowchart TD
  classDef cmd fill:#bfe0ff,stroke:#2b78c2,color:#000
  classDef ev fill:#ffd9a0,stroke:#c77d00,color:#000
  classDef po fill:#dcc8ff,stroke:#7b4fd1,color:#000
  classDef se fill:#ffb6dc,stroke:#c2417c,color:#000
  C(["Cobrança enviada ao cliente"]) -.-> D1["PO Quando passarem 5 dias úteis da finalização sem pagamento registrado, identificar como em atraso"]:::po -.-> D2["CMD Identifica pagamento em atraso"]:::cmd -.-> D3["EV Pagamento em atraso"]:::ev
  D3 -.-> D4["PO Enviar notificação informando que o pagamento não foi efetuado dentro do prazo"]:::po -.-> D5["CMD Notifica cliente e funcionários sobre o atraso no pagamento"]:::cmd -.-> D6["EV Cliente e funcionários notificados do pagamento em atraso"]:::ev
  D5 --- D5S["SE Sistema de envio de e-mail"]:::se
  C -.-> D7
  D6 -.-> D7["PO Identificar como vencido se não houver pagamento registrado e passarem 5 dias úteis da notificação de atraso"]:::po -.-> D8["CMD Identifica pagamento como vencido"]:::cmd -.-> D9["EV Pagamento vencido"]:::ev
  D9 -.-> D10["PO Cancelar OS por falta de pagamento"]:::po -.-> D11["CMD Atualiza OS para cancelada"]:::cmd -.-> D12["EV OS atualizada para cancelada por falta de pagamento"]:::ev
  D12 -.-> D13["PO Suspender cliente por inadimplência"]:::po -.-> D14["CMD Atualiza cadastro do cliente como inadimplente"]:::cmd -.-> D15["EV Cliente suspenso de solicitar novos serviços por inadimplência"]:::ev
  D15 -.-> D16["PO Notificar os funcionários de cancelamento de OS por inadimplência"]:::po -.-> D17["CMD Envia notificação de OS cancelada"]:::cmd -.-> D18["EV Funcionário notificado da OS cancelada por falta de pagamento do cliente"]:::ev
  D18 -.-> D19["CMD Desfaz o serviço"]:::cmd -.-> D20["EV Serviço desfeito"]:::ev -.-> D21["CMD Atualiza estoque de peças"]:::cmd -.-> D22["EV Peças e Insumos retornadas ao estoque"]:::ev
  D21 --- D21S["SE Sistema de gestão de peças e insumos"]:::se
  D22 -.-> FIM(["política Enviar notificação para retirada do veículo, no caminho principal"])
```

| # | Ator | Comando | Evento | Política associada | Sistema externo |
|---|---|---|---|---|---|
| D1 | n/a | Identifica pagamento em atraso | Pagamento em atraso | Antes: quando passarem 5 dias úteis da finalização da OS sem pagamento registrado, identificar o pagamento como em atraso. Depois: enviar notificação informando que o pagamento não foi efetuado dentro do prazo | n/a |
| D2 | n/a | Notifica cliente e funcionários sobre o atraso no pagamento | Cliente e funcionários notificados do pagamento em atraso | n/a | Sistema de envio de e-mail |
| D3 | n/a | Identifica pagamento como vencido | Pagamento vencido | Antes: identificar como vencido se não houver pagamento registrado e passarem 5 dias úteis da notificação de atraso. Depois: cancelar a OS por falta de pagamento | n/a |
| D4 | n/a | Atualiza OS para "cancelada" | OS atualizada para o status "cancelada" por falta de pagamento | Suspender cliente por inadimplência | n/a |
| D5 | n/a | Atualiza cadastro do cliente como inadimplente | Cliente suspenso de solicitar novos serviços por inadimplência | Notificar os funcionários de cancelamento de OS por inadimplência | n/a |
| D6 | n/a | Envia notificação de OS cancelada | Funcionário notificado da OS cancelada por falta de pagamento do cliente | n/a | n/a |
| D7 | Funcionário | Desfaz o serviço | Serviço desfeito | n/a | n/a |
| D8 | Funcionário | Atualiza estoque de peças | Peças e Insumos retornadas ao estoque | Depois: enviar notificação para retirada do veículo (entra no passo 7 do caminho principal) | Sistema de gestão de peças e insumos |

O *Cliente suspenso por inadimplência* (D5) é o mesmo estado consultado na fase 1, quando o pedido de serviço é rejeitado por suspensão do cliente.

## 6. Agregados (caminho principal)

Os comandos, eventos e políticas do caminho principal agrupados por agregado. As fases seguem a ordem Criação da OS, Serviço e Pagamento. As exceções não entram nesta visão.

Sequência dos agregados: **Cliente → OS → Orçamento → Serviço → Pagamento → Finaliza Serviço**.

**Cliente** (comandos; evento resultante; política)

| Comando | Evento | Política ou observação |
|---|---|---|
| Solicita um serviço (Cliente; ML Lista de serviços disponíveis) | Pedido de serviço solicitado | Validar o CPF/CNPJ de acordo com as regras da Receita Federal |
| Valida CPF/CNPJ | CPF/CNPJ validado | n/a |
| Identifica cliente (Funcionário; ML Lista de clientes cadastrados) | Cliente identificado | Validar o formato da placa; se inválida, rejeitar |
| Valida placa | Placa do veículo validada | n/a |
| Identifica veículo (Funcionário; ML Lista de veículos cadastrados) | Veículo identificado | n/a |

**OS**

| Comando | Evento | Política ou observação |
|---|---|---|
| Cria Ordem de Serviço (Funcionário) | Ordem de serviço criada com status "recebida" | O Sistema atualiza a OS para "em diagnóstico" |
| Atualiza OS para "em diagnóstico" | Ordem de serviço atualizada para o status "em diagnóstico" | n/a |
| Adiciona serviços, peças e insumos à OS (Funcionário; ML Lista de serviços com peças e insumos necessários) | Serviços, peças e insumos adicionados à OS | Sistema externo: Sistema de estoque de itens. Política entre OS e Orçamento: somatório simples do valor padrão dos serviços e dos valores de peças e insumos recuperados do contexto de estoque |

**Orçamento**

| Comando | Evento | Política ou observação |
|---|---|---|
| Calcula orçamento do serviço | Orçamento do serviço calculado | Notificar contendo os detalhes do orçamento |
| Envia orçamento para o cliente | Orçamento enviado ao cliente | SE e-mail. O Sistema atualiza a OS para "aguardando aprovação" |
| Atualiza OS para "aguardando aprovação" | OS atualizada para o status "aguardando aprovação" | n/a |
| Aprova o orçamento (Cliente; ML Lista dos componentes do orçamento da OS) | Orçamento aprovado | Notificar o funcionário sobre a aprovação do orçamento |
| Notifica funcionário sobre a aprovação do orçamento | Funcionário notificado sobre aprovação do orçamento | Depois: O Sistema atualiza a OS para "em execução" (política entre Orçamento e Serviço) |

**Serviço**

| Comando | Evento | Política ou observação |
|---|---|---|
| Atualiza OS para "em execução" | OS atualizada para o status "em execução" | Notificar o cliente que a OS está em execução |
| Notifica o cliente que a OS está em execução | Cliente notificado que a OS está em execução | SE e-mail |
| Inicia serviço (Funcionário; ML Lista dos serviços da OS) | Serviço iniciado | n/a |
| Finaliza serviço (Funcionário; ML Lista dos serviços da OS) | Serviço finalizado | Atualizar o estado da OS para "finalizada" |
| Atualiza OS para "finalizada" | OS atualizada com status de "finalizada" | Notificar o cliente que a OS está finalizada (liga ao agregado Pagamento) |

**Pagamento**

| Comando | Evento | Política ou observação |
|---|---|---|
| Notifica o cliente que a OS está finalizada | Cliente notificado da finalização da OS | SE e-mail. Quando o cliente for notificado, enviar a cobrança |
| Notifica o cliente de cobrança pendente | Cobrança enviada ao cliente | SE e-mail |
| Efetua pagamento (Cliente; ML Detalhamento do orçamento e instruções para pagamento) | Pagamento efetuado | SE sistema de gestão de pagamentos. Quando o pagamento for efetuado, registrar na OS |
| Registra pagamento na OS | Pagamento registrado na OS | Emitir nota fiscal após pagamento |
| Emite Nota Fiscal | Nota fiscal emitida | Notificar com a nota fiscal para o cliente |
| Envia Nota Fiscal ao cliente | Nota fiscal enviada ao cliente | SE e-mail. Enviar notificação para retirada do veículo (liga ao agregado Finaliza Serviço) |

**Finaliza Serviço**

| Comando | Evento | Política ou observação |
|---|---|---|
| Envia notificação de retirada | Cliente notificado para a retirada do veículo | Atualizar a OS para entregue |
| Entrega o veículo ao cliente (Funcionário) | Veículo entregue ao cliente | n/a |
| Atualiza OS para o status "entregue" (Funcionário) | OS atualizada para o status "entregue" | n/a |

## 7. Contextos e integrações

Os agregados são agrupados em três bounded contexts:

```mermaid
flowchart LR
  subgraph C1["Gestão de Clientes e Veículos"]
    Cliente
  end
  subgraph C2["Gestão de Ordens de Serviço"]
    OS --> Orcamento["Orçamento"] --> Servico["Serviço"]
    Finaliza["Finaliza Serviço"]
  end
  subgraph C3["Cobrança e Faturamento"]
    Pagamento
  end
  Cliente --> OS
  Servico -->|OS finalizada| Pagamento
  Pagamento -->|enviar notificação de retirada| Finaliza
  OS -.consulta.-> Estoque["SE Sistema de estoque de itens"]
  Orcamento -.e-mail.-> Email["SE Sistema de envio de e-mail"]
  Servico -.e-mail.-> Email
  Pagamento -.e-mail.-> Email
  Pagamento -.-> Gestao["SE Sistema de gestão de pagamentos"]
```

| Contexto | Agregados |
|---|---|
| Gestão de Clientes e Veículos | Cliente |
| Gestão de Ordens de Serviço | OS, Orçamento, Serviço, Finaliza Serviço |
| Cobrança e Faturamento | Pagamento |

As integrações entre contextos ligam a política "Notificar o cliente que a OS está finalizada" (Serviço) ao comando "Notifica o cliente que a OS está finalizada" (Pagamento), e a política "Enviar notificação para retirada do veículo" (Pagamento) ao comando "Envia notificação de retirada" (Finaliza Serviço). O agregado Finaliza Serviço e a política "Enviar notificação para retirada do veículo" ligam este contexto de volta ao de Ordens de Serviço.
