# Fluxo de Estoque e Itens da OS: descrição completa do Event Storming

O documento cobre, na ordem: atores e sistemas externos, cada fase (com diagramas e tabelas passo a passo), os desvios e exceções, os agregados, o contexto e a relação com o fluxo da OS.

Os diagramas estão em Mermaid. As tabelas repetem o mesmo conteúdo em texto.

Nos diagramas Mermaid deste documento: caixa **azul** = comando (CMD), **laranja** = evento (EV), **roxa** = política (PO), **rosa** = sistema externo (SE). Linha **contínua** = caminho principal; linha **tracejada** = exceção ou retorno. Atores e modelos de leitura não aparecem nos diagramas e estão nas tabelas.

## 1. Atores, modelos de leitura e sistemas externos

**Ator:** só o **Funcionário** (cadastra itens, preenche dados, orça com fornecedores, descarta insumos, recebe itens do fornecedor, registra peça defeituosa, aciona garantia). O Cliente não participa deste fluxo: as decisões dele chegam ao estoque como notificações vindas da OS ("o orçamento foi aprovado").

**Sistemas externos (SE):**

| Sistema | Onde é usado |
|---|---|
| Sistema de gestão de ordens de serviço | Origem de todas as notificações da OS (itens pedidos, orçamento aprovado ou rejeitado, OS em execução, finalizada, entregue ou cancelada). É o outro lado do fluxo da OS |
| Sistema de fornecedor de itens | Orçamento entre fornecedores, novos pedidos, comparação de preços, verificação de itens fora de linha e garantia |

**Modelos de leitura (ML):** Lista de itens do estoque; Lista de itens indisponíveis; Lista de insumos vinculados à OS; Lista de insumos marcados para descarte; Lista de itens que constam no orçamento do fornecedor; Lista de peças vinculadas à OS; Lista de peças do estoque.

## 2. Visão geral das cinco fases

| Fase | Nome | Resumo |
|---|---|---|
| 1 | Solicitação de itens | A OS pede itens; itens novos são cadastrados; o estoque também recebe entradas manuais |
| 2 | Vínculo de itens aos serviços | Os itens pedidos são vinculados à OS (ou desvinculados) |
| 3 | Disponibilidade e preço | O sistema verifica se há saldo, devolve o preço ao orçamento e trata indisponibilidade, fornecedores e insumos comprometidos |
| 4 | Reserva | O orçamento é aprovado (total, parcial) ou rejeitado; itens faltantes são pedidos ao fornecedor; os itens são reservados |
| 5 | Uso dos itens no serviço | Acompanha o status da OS (em execução, finalizada, entregue, cancelada), baixa o estoque e trata peça defeituosa |

```mermaid
flowchart LR
  F1["1 Solicitação de itens"] --> F2["2 Vínculo de itens aos serviços"] --> F3["3 Disponibilidade e preço"] --> F4["4 Reserva"] --> F5["5 Uso dos itens no serviço"]
  F2 -. "item removido: reverifica saldo" .-> F3
  F4 -. "peça defeituosa volta ao fluxo" .-> F5
```

Em uma frase: a OS pede itens, o estoque vincula e verifica a disponibilidade, devolve o preço ao orçamento, e **só depois que o orçamento é aprovado** reserva os itens. Quando a OS avança (em execução, finalizada, entregue), o estoque dá baixa nos saldos. Se a OS é cancelada ou aparece uma peça defeituosa, existem trilhas próprias.

## 3. Fase 1: Solicitação de itens

A fase tem duas trilhas. A de cima começa com a OS pedindo itens; a de baixo é a entrada manual de itens no estoque. As duas se encontram no cadastro de item novo.

```mermaid
flowchart TD
  P1["PO Receber notificação de atualização de itens na OS"] --> C1["CMD Adicionar peças e insumos a OS"]
  S1["SE Sistema de gestão de ordens de serviço"] <--> C1
  C1 --> E1["EV Itens da OS solicitados ou atualizados"]
  E1 -->|"item não cadastrado"| C2["CMD Cadastrar item no sistema"]
  C2 --> E2["EV Cadastro de novo item no estoque"]
  E2 --> P2["PO Verificar existência de item duplicado na base de dados"] --> C3["CMD Verificar se novo item é duplicata de item já registrado"]
  C3 -.-> E3["EV Cadastro de item rejeitado por SKU duplicado"]
  E1 --> F2["segue para a Fase 2: PO Efetuar a vinculação"]
  E2 --> F2b["segue para a Fase 2: CMD Vincular item à OS"]

  M1["CMD Adicionar peças ou insumos ao estoque"] --> M2["EV Estoque atualizado"] --> M3["CMD Preenchimento de dados dos itens"] --> M4["EV Entrada manual de itens registrada"]
  M4 --> M5["PO Atualizar quantidade do saldo físico do estoque"] --> M6["CMD Atualiza itens no estoque"] --> M7["EV Saldo físico do item atualizado"]
  M4 --> M8["CMD Cadastrar item no sistema"] --> M9["EV Cadastro de novo item no estoque"]
  M9 --> M10["PO Verificar existência de item duplicado na base de dados"] --> M11["CMD Verificar se novo item é duplicata de item já registrado"]
  M11 -.-> M12["EV Cadastro de item rejeitado por SKU duplicado"]

  classDef cmd fill:#bfe0ff,stroke:#2b78c2,color:#000
  classDef ev fill:#ffd9a0,stroke:#c77d00,color:#000
  classDef po fill:#dcc8ff,stroke:#7b4fd1,color:#000
  classDef se fill:#ffb6dc,stroke:#c2417c,color:#000
  class C1,C2,C3,M1,M3,M6,M8,M11 cmd
  class E1,E2,E3,M2,M4,M7,M9,M12 ev
  class P1,P2,M5,M10 po
  class S1 se
```

**Trilha de cima: a OS pede itens**

| # | Ator e modelo de leitura | Comando | Evento | Política associada | Sistema externo |
|---|---|---|---|---|---|
| 1 | (nenhum) | Adicionar peças e insumos a OS | Itens da OS solicitados ou atualizados | Antes: Receber notificação de atualização de itens na OS (gatilho que inicia o fluxo) | Sistema de gestão de ordens de serviço |
| 2 | Funcionário | Cadastrar item no sistema (quando o item pedido ainda não existe) | Cadastro de novo item no estoque | Depois: Verificar existência de item duplicado na base de dados | n/a |
| 3 | n/a | Verificar se novo item é duplicata de item já registrado | (exceção) Cadastro de item rejeitado por SKU duplicado | n/a | n/a |

**Trilha de baixo: entrada manual no estoque**

| # | Ator e modelo de leitura | Comando | Evento | Política associada | Sistema externo |
|---|---|---|---|---|---|
| 4 | Funcionário; ML Lista de itens do estoque | Adicionar peças ou insumos ao estoque | Estoque atualizado | n/a | n/a |
| 5 | Funcionário | Preenchimento de dados dos itens | Entrada manual de itens registrada | n/a | n/a |
| 6 | n/a | Atualiza itens no estoque | Saldo físico do item atualizado | Antes: Atualizar quantidade do saldo físico do estoque | n/a |
| 7 | Funcionário | Cadastrar item no sistema | Cadastro de novo item no estoque | Depois: Verificar existência de item duplicado na base de dados | n/a |
| 8 | n/a | Verificar se novo item é duplicata de item já registrado | (exceção) Cadastro de item rejeitado por SKU duplicado | n/a | n/a |

Os passos 7 e 8 repetem os passos 2 e 3: o mesmo cadastro de item novo é alcançado tanto pelo pedido da OS quanto pela entrada manual. **Os dois eventos "Cadastro de item rejeitado por SKU duplicado" são pontos finais**: nada acontece depois deles.

## 4. Fase 2: Vínculo de itens aos serviços

```mermaid
flowchart TD
  E1["EV Itens da OS solicitados ou atualizados (fase 1)"] --> P1["PO Efetuar a vinculação dos itens que foram solicitados pelo funcionário à OS"]
  E0["EV Cadastro de novo item no estoque (fase 1)"] --> C1
  P1 --> C1["CMD Vincular item à OS"] --> E2["EV Item vinculado a OS"]
  E2 -.-> P2["PO Remover o vínculo dos itens que removidos pelo funcionário"] -.-> C2["CMD Desvincular item da OS"] -.-> E3["EV Vínculo do item com OS removido"]
  E2 --> N["segue para a Fase 3: PO Verificar o saldo físico..."]
  E3 -.-> N

  classDef cmd fill:#bfe0ff,stroke:#2b78c2,color:#000
  classDef ev fill:#ffd9a0,stroke:#c77d00,color:#000
  classDef po fill:#dcc8ff,stroke:#7b4fd1,color:#000
  class C1,C2 cmd
  class E0,E1,E2,E3 ev
  class P1,P2 po
```

| # | Ator e modelo de leitura | Comando | Evento | Política associada | Sistema externo |
|---|---|---|---|---|---|
| 1 | n/a | Vincular item à OS | Item vinculado a OS | Antes: Efetuar a vinculação dos itens que foram solicitados pelo funcionário à OS | n/a |
| 2 | n/a | (desvio) Desvincular item da OS | (desvio) Vínculo do item com OS removido | Antes: Remover o vínculo dos itens que foram removidos pelo funcionário | n/a |

O evento "Item vinculado a OS" também dispara a verificação de saldo da fase 3. O evento "Vínculo do item com OS removido" volta, em seta vermelha tracejada, para a mesma verificação de saldo da fase 3: depois de remover um item, a disponibilidade é calculada de novo.

## 5. Fase 3: Disponibilidade e preço

Esta é a fase mais densa. Ela tem uma linha principal (verificar saldo e devolver o preço) e três desvios em vermelho: **item indisponível**, **item fora de linha** e **insumo comprometido**.

**5.1 Caminho principal**

```mermaid
flowchart TD
  P1["PO Verificar o saldo físico dos itens subtraindo o saldo de itens reservados para verificar a disponibilidade"] --> C1["CMD Verificar a disponibilidade dos itens vinculados à OS"]
  C1 --> E1["EV Disponibilidade de itens da OS consultada"]
  E1 --> P2["PO Recuperar o preço de venda registrado no sistema para envio"] --> C2["CMD Enviar o preço de venda dos itens ao orçamento"]
  C2 --> E2["EV Preço de venda dos itens vinculados à OS retornado ao orçamento"]
  E2 --> F["segue para a Fase 4: notificações de orçamento"]
  C0["EV Item vinculado a OS (fase 2)"] --> P1

  classDef cmd fill:#bfe0ff,stroke:#2b78c2,color:#000
  classDef ev fill:#ffd9a0,stroke:#c77d00,color:#000
  classDef po fill:#dcc8ff,stroke:#7b4fd1,color:#000
  class C1,C2 cmd
  class C0,E1,E2 ev
  class P1,P2 po
```

| # | Ator e modelo de leitura | Comando | Evento | Política associada | Sistema externo |
|---|---|---|---|---|---|
| 1 | n/a | Verificar a disponibilidade dos itens vinculados à OS | Disponibilidade de itens da OS consultada | Antes: Verificar o saldo físico dos itens subtraindo o saldo de itens reservados para verificar a disponibilidade | n/a |
| 2 | n/a | Enviar o preço de venda dos itens ao orçamento | Preço de venda dos itens vinculados à OS retornado ao orçamento | Antes: Recuperar o preço de venda registrado no sistema para envio | n/a |

A disponibilidade é **saldo físico menos saldo reservado**. O preço usado é o preço de venda registrado no sistema.

**5.2 Desvio A: item indisponível (cotação com fornecedores)**

Sai do evento "Disponibilidade de itens da OS consultada" em seta vermelha tracejada.

```mermaid
flowchart TD
  E0["EV Disponibilidade de itens da OS consultada"] -.-> P1["PO Atualizar estado dos itens vinculados para indisponível"] -.-> C1["CMD Identificar itens vinculados como indisponíveis no estoque"] -.-> E1["EV Indisponibilidade de item identificada"]
  E1 -.-> P2["PO Enviar notificação no sistema para o funcionário responsável pela OS sobre a indisponibilidade dos itens"] -.-> C2["CMD Enviar notificação sobre indisponibilidade de itens no estoque"] -.-> E2["EV Funcionário notificado sobre indisponibilidade"]
  E2 -.-> C3["CMD Orçar o valor dos itens entre diferentes fornecedores para obter os itens indisponíveis"] -.-> E3["EV Efetuado orçamento de fornecedores de itens"]
  S1["SE Sistema de fornecedor de itens"] --> C3
  E3 -.-> R["alimenta o envio do preço ao orçamento (5.1)"]
  E1 -.-> C4["CMD Verificar disponibilidade de item com fornecedores"] -.-> E4["EV Item identificado como fora de linha"]
  E4 -.-> C5["CMD Remover item fora de linha da OS"] -.-> E5["EV Lista de itens vinculados a OS modificada"]
  E5 -.-> P3["PO Inativar o item que foi identificado como fora de linha"] -.-> C6["CMD Inativar item indisponível"] -.-> E6["EV Item indisponivel inativado"]
  S2["SE Sistema de fornecedor de itens"] --> C4

  classDef cmd fill:#bfe0ff,stroke:#2b78c2,color:#000
  classDef ev fill:#ffd9a0,stroke:#c77d00,color:#000
  classDef po fill:#dcc8ff,stroke:#7b4fd1,color:#000
  classDef se fill:#ffb6dc,stroke:#c2417c,color:#000
  class C1,C2,C3,C4,C5,C6 cmd
  class E0,E1,E2,E3,E4,E5,E6 ev
  class P1,P2,P3 po
  class S1,S2 se
```

| # | Ator e modelo de leitura | Comando | Evento | Política associada | Sistema externo |
|---|---|---|---|---|---|
| 1 | n/a | Identificar itens vinculados como indisponíveis no estoque | Indisponibilidade de item identificada | Antes: Atualizar estado dos itens vinculados para indisponível | n/a |
| 2 | n/a | Enviar notificação sobre indisponibilidade de itens no estoque | Funcionário notificado sobre indisponibilidade | Antes: Enviar notificação no sistema para o funcionário responsável pela OS sobre a indisponibilidade dos itens | n/a |
| 3 | Funcionário; ML Lista de itens indisponíveis | Orçar o valor dos itens entre diferentes fornecedores para obter os itens indisponíveis | Efetuado orçamento de fornecedores de itens | n/a | Sistema de fornecedor de itens |
| 4 | Funcionário; ML Lista de itens indisponíveis | Verificar disponibilidade de item com fornecedores | Item identificado como fora de linha | n/a | Sistema de fornecedor de itens |
| 5 | Funcionário; ML Lista de itens indisponíveis | Remover item fora de linha da OS | Lista de itens vinculados a OS modificada | n/a | n/a |
| 6 | n/a | Inativar item indisponível | Item indisponivel inativado | Antes: Inativar o item que foi identificado como fora de linha | n/a |

Leitura do desvio: quando falta item, o sistema o marca como indisponível, avisa o funcionário e abre duas frentes: cotar com fornecedores (passo 3, cujo resultado volta para o cálculo do preço) e descobrir se o item saiu de linha (passos 4 a 6: se saiu, é removido da OS e inativado no cadastro).

**5.3 Desvio B: insumo comprometido (integridade física e validade)**

Sai do mesmo evento "Disponibilidade de itens da OS consultada" por uma seta vermelha tracejada que desce até a trilha de baixo.

```mermaid
flowchart TD
  E0["EV Disponibilidade de itens da OS consultada"] -.-> C1["CMD Verificar a integridade física e validade dos insumos"]
  M0["ML Lista de insumos vinculados à OS"] --> C1
  C1 --> E1["EV Insumos com integridade comprometida identificados"]
  E1 --> P1["PO Marcar os insumos que estão comprometidos para descarte posterior"]
  E1 --> C2["CMD Atualizar insumos como marcados para descarte"] --> E2["EV Insumos comprometidos marcados para descarte"]
  E2 --> C3["CMD Descartar insumos que foram marcados para descarte"] --> E3["EV Insumos comprometidos descartados"]
  E3 --> P2["PO Subtrair do saldo físico dos itens a quantidade de itens que foram descartados"]
  E3 --> C4["CMD Descartar insumos que foram marcados para descarte (2ª ocorrência)"] --> E4["EV Saldo físico dos insumos atualizado"]

  classDef cmd fill:#bfe0ff,stroke:#2b78c2,color:#000
  classDef ev fill:#ffd9a0,stroke:#c77d00,color:#000
  classDef po fill:#dcc8ff,stroke:#7b4fd1,color:#000
  class C1,C2,C3,C4 cmd
  class E0,E1,E2,E3,E4 ev
  class P1,P2 po
```

| # | Ator e modelo de leitura | Comando | Evento | Política associada | Sistema externo |
|---|---|---|---|---|---|
| 1 | Funcionário; ML Lista de insumos vinculados à OS | Verificar a integridade física e validade dos insumos | Insumos com integridade comprometida identificados | Depois: Marcar os insumos que estão comprometidos para descarte posterior | n/a |
| 2 | n/a | Atualizar insumos como marcados para descarte | Insumos comprometidos marcados para descarte | n/a | n/a |
| 3 | Funcionário; ML Lista de insumos marcados para descarte | Descartar insumos que foram marcados para descarte | Insumos comprometidos descartados | Depois: Subtrair do saldo físico dos itens a quantidade de itens que foram descartados | n/a |
| 4 | n/a | Descartar insumos que foram marcados para descarte | Saldo físico dos insumos atualizado | n/a | n/a |

## 6. Fase 4: Reserva

A fase começa quando o **orçamento recebe o preço** (fim da fase 3). A OS notifica o estoque da decisão do cliente: aprovado parcialmente, rejeitado ou aprovado. Só a aprovação leva à reserva.

**6.1 Decisão sobre o orçamento**

```mermaid
flowchart TD
  X["EV Preço de venda ... retornado ao orçamento (fase 3)"] --> PA["PO Receber notificação que o orçamento foi aprovado parcialmente"]
  X --> PB["PO Receber notificação que o orçamento foi rejeitado"]
  X --> PC["PO Receber notificação que o orçamento foi aprovado"]
  PA --> CA["CMD Aprova orçamento parcialmente"] --> EA["EV Orçamento é aprovado parcialmente"]
  PB --> CB["CMD Rejeita orçamento"] --> EB["EV Orçamento é rejeitado"]
  PC --> CC["CMD Aprova orçamento"] --> EC["EV Orçamento aprovado"]
  S["SE Sistema de gestão de ordens de serviço"] --> CA
  S --> CB
  S --> CC
  EA --> RES["CMD Atualiza o saldo dos itens que foram reservados (ver 6.3)"]
  EC --> RES

  classDef cmd fill:#bfe0ff,stroke:#2b78c2,color:#000
  classDef ev fill:#ffd9a0,stroke:#c77d00,color:#000
  classDef po fill:#dcc8ff,stroke:#7b4fd1,color:#000
  classDef se fill:#ffb6dc,stroke:#c2417c,color:#000
  class CA,CB,CC,RES cmd
  class X,EA,EB,EC ev
  class PA,PB,PC po
  class S se
```

| # | Ator e modelo de leitura | Comando | Evento | Política associada | Sistema externo |
|---|---|---|---|---|---|
| 1 | n/a | Aprova orçamento parcialmente | Orçamento é aprovado parcialmente | Antes: Receber notificação que o orçamento foi aprovado parcialmente | Sistema de gestão de ordens de serviço |
| 2 | n/a | Rejeita orçamento | Orçamento é rejeitado | Antes: Receber notificação que o orçamento foi rejeitado | Sistema de gestão de ordens de serviço |
| 3 | n/a | Aprova orçamento | Orçamento aprovado | Antes: Receber notificação que o orçamento foi aprovado | Sistema de gestão de ordens de serviço |

Os eventos "Orçamento é aprovado parcialmente" e "Orçamento aprovado" seguem para a atualização do saldo reservado (6.3). "Orçamento é rejeitado" **não tem nenhuma continuação** no fluxo.

**6.2 Item em falta depois da aprovação: pedido ao fornecedor**

A partir de "Orçamento aprovado" o fluxo repete a verificação de disponibilidade. Se faltar item, abre-se uma trilha de compra com o fornecedor.

```mermaid
flowchart TD
  EC["EV Orçamento aprovado"] --> P1["PO Verificar o saldo físico dos itens subtraindo o saldo de itens reservados para verificar a disponibilidade"] --> C1["CMD Verificar a disponibilidade dos itens vinculados à OS"] --> E1["EV Indisponibilidade de itens no estoque"]
  S0["SE Sistema de gestão de ordens de serviço"] --> E1
  E1 --> C2["CMD Efetua novos pedidos de itens junto ao fornecedor"] --> E2["EV Novos itens pedidos ao fornecedor"]
  E2 --> C3["CMD Recebe os itens que foram enviados pelo fornecedor"] --> E3["EV Itens pedidos são entregues pelo fornecedor"]
  E3 --> P2["PO Adicionar os novos itens que foram entregues pelo fornecedor no estoque"] --> C4["CMD Atualiza o saldo físico de itens no estoque"] --> E4["EV Saldo físico de itens no estoque atualizado"]
  E4 --> RES["CMD Atualiza o saldo dos itens que foram reservados (6.3)"]
  EC --> C5["CMD Compara os preços definidos no orçamento com o preço pedido pelo fornecedor"] --> E5["EV Orçamento de fornecedor desatualizado"]
  E5 --> C6["CMD Refaz o orçamento dos novos itens junto aos fornecedores"] --> E6["EV Orçamento com fornecedores de itens refeito"]
  E6 -.->|"volta para refazer o pedido"| C2

  classDef cmd fill:#bfe0ff,stroke:#2b78c2,color:#000
  classDef ev fill:#ffd9a0,stroke:#c77d00,color:#000
  classDef po fill:#dcc8ff,stroke:#7b4fd1,color:#000
  classDef se fill:#ffb6dc,stroke:#c2417c,color:#000
  class C1,C2,C3,C4,C5,C6,RES cmd
  class EC,E1,E2,E3,E4,E5,E6 ev
  class P1,P2 po
  class S0 se
```

| # | Ator e modelo de leitura | Comando | Evento | Política associada | Sistema externo |
|---|---|---|---|---|---|
| 1 | n/a | Verificar a disponibilidade dos itens vinculados à OS | Indisponibilidade de itens no estoque | Antes: Verificar o saldo físico dos itens subtraindo o saldo de itens reservados para verificar a disponibilidade | Sistema de gestão de ordens de serviço |
| 2 | Funcionário; ML Lista de itens que constam no orçamento do fornecedor | Efetua novos pedidos de itens junto ao fornecedor | Novos itens pedidos ao fornecedor | n/a | Sistema de fornecedor de itens |
| 3 | Funcionário | Recebe os itens que foram enviados pelo fornecedor | Itens pedidos são entregues pelo fornecedor | n/a | n/a |
| 4 | n/a | Atualiza o saldo físico de itens no estoque | Saldo físico de itens no estoque atualizado | Antes: Adicionar os novos itens que foram entregues pelo fornecedor no estoque | n/a |
| 5 | Funcionário; ML Lista de itens que constam no orçamento do fornecedor | Compara os preços definidos no orçamento com o preço pedido pelo fornecedor | Orçamento de fornecedor desatualizado | n/a | Sistema de fornecedor de itens |
| 6 | Funcionário; ML Lista de itens que constam no orçamento do fornecedor | Refaz o orçamento dos novos itens junto aos fornecedores | (exceção, seta vermelha de volta ao passo 2) Orçamento com fornecedores de itens refeito | n/a | n/a |

Este é um **laço**: se o preço do fornecedor mudou em relação ao orçamento, o orçamento é refeito e o pedido volta a ser feito. Depois da entrega (passo 4), o saldo físico atualizado desemboca na reserva.

**6.3 Reserva dos itens**

```mermaid
flowchart TD
  A["EV Orçamento aprovado / Orçamento é aprovado parcialmente / Saldo físico de itens no estoque atualizado"] --> P1["PO Efetua a reserva dos itens que constam no orçamento aprovado"]
  P1 --> C1["CMD Atualiza o saldo dos itens que foram reservados"] --> E1["EV Itens da OS reservados"]
  E1 --> P2["PO Soma a quantidade dos novos itens que foram reservados no saldo de cada item"] --> C2["CMD Atualiza o saldo dos itens que foram reservados"] --> E2["EV Saldo reservado do item atualizado"]
  E2 --> F["segue para a Fase 5"]

  classDef cmd fill:#bfe0ff,stroke:#2b78c2,color:#000
  classDef ev fill:#ffd9a0,stroke:#c77d00,color:#000
  classDef po fill:#dcc8ff,stroke:#7b4fd1,color:#000
  class C1,C2 cmd
  class A,E1,E2 ev
  class P1,P2 po
```

| # | Ator e modelo de leitura | Comando | Evento | Política associada | Sistema externo |
|---|---|---|---|---|---|
| 1 | n/a | Atualiza o saldo dos itens que foram reservados | Itens da OS reservados | Antes: Efetua a reserva dos itens que constam no orçamento aprovado | n/a |
| 2 | n/a | Atualiza o saldo dos itens que foram reservados | Saldo reservado do item atualizado | Antes: Soma a quantidade dos novos itens que foram reservados no saldo de cada item | n/a |

Os dois comandos atualizam a reserva. O primeiro registra a reserva dos itens da OS; o segundo, pela política que o precede, soma a quantidade reservada ao saldo reservado de cada item.

## 7. Fase 5: Uso dos itens no serviço

A fase acompanha as mudanças de status da OS, todas recebidas como notificação do sistema de gestão de ordens de serviço. Há uma linha principal no meio, uma trilha de **cancelamento** em cima e uma trilha de **peça defeituosa** embaixo.

**7.1 Caminho principal: execução, finalização e entrega**

```mermaid
flowchart TD
  X["EV Saldo reservado do item atualizado (fase 4)"] --> C1["CMD Receber notificação de atualização da OS para em execução"]
  S1["SE Sistema de gestão de ordens de serviço"] --> C1
  C1 --> E1["EV OS atualizada para em execução"]
  E1 --> C2["CMD Receber notificação de atualização da OS para finalizada"]
  P0["PO Receber notificação da OS"] --> C2
  C2 --> E2["EV OS atualizada para finalizada"]
  E2 --> P1["PO Atualizar quantidade do saldo físico do estoque"] --> C3["CMD Atualiza itens no estoque"] --> E3["EV Saldo físico do item atualizado"]
  E3 --> C4["CMD Receber notificação de atualização da OS para entregue"] --> E4["EV OS atualizada para entregue"]
  S2["SE Sistema de gestão de ordens de serviço"] --> C4
  E3 --> P2["PO Identificação de estoque abaixo do mínimo esperado"] --> C5["CMD Enviar notificação para atualização de estoque"] --> E5["EV Funcionário notificado sobre a necessidade de atualizar o estoque"]
  E5 --> P3["PO Receber notificação da OS"] --> C4

  classDef cmd fill:#bfe0ff,stroke:#2b78c2,color:#000
  classDef ev fill:#ffd9a0,stroke:#c77d00,color:#000
  classDef po fill:#dcc8ff,stroke:#7b4fd1,color:#000
  classDef se fill:#ffb6dc,stroke:#c2417c,color:#000
  class C1,C2,C3,C4,C5 cmd
  class X,E1,E2,E3,E4,E5 ev
  class P0,P1,P2,P3 po
  class S1,S2 se
```

| # | Ator e modelo de leitura | Comando | Evento | Política associada | Sistema externo |
|---|---|---|---|---|---|
| 1 | n/a | Receber notificação de atualização da OS para "em execução" | OS atualizada para "em execução" | n/a | Sistema de gestão de ordens de serviço |
| 2 | n/a | Receber notificação de atualização da OS para "finalizada" | OS atualizada para "finalizada" | Antes: Receber notificação da OS | Sistema de gestão de ordens de serviço |
| 3 | n/a | Atualiza itens no estoque | Saldo físico do item atualizado | Antes: Atualizar quantidade do saldo físico do estoque (baixa dos itens consumidos) | n/a |
| 4 | n/a | Enviar notificação para atualização de estoque | Funcionário notificado sobre a necessidade de atualizar o estoque | Antes: Identificação de estoque abaixo do mínimo esperado | n/a |
| 5 | n/a | Receber notificação de atualização da OS para "entregue" | OS atualizada para "entregue" | Antes: Receber notificação da OS | Sistema de gestão de ordens de serviço |

A baixa do estoque acontece **quando a OS é finalizada**, não quando o serviço começa. Depois da baixa, se o saldo ficou abaixo do mínimo, o funcionário é notificado. A OS "entregue" é o último evento do caminho principal.

**7.2 Desvio: OS cancelada**

Sai de "OS atualizada para finalizada" por uma seta vermelha tracejada que sobe até a trilha de cima.

```mermaid
flowchart TD
  E0["EV OS atualizada para finalizada"] -.-> P1["PO Receber notificação da OS"] -.-> C1["CMD Receber notificação de atualização da OS cancelada"]
  S["SE Sistema de gestão de ordens de serviço"] --> C1
  C1 --> E1["EV OS atualizada para cancelada"]
  E1 --> C2["CMD Atualiza peças recuperadas"] --> E2["EV Peças recuperadas e retornadas ao estoque"]
  E2 --> C3["CMD Atualiza peças não recuperadas"] --> E3["EV Perda ou avaria de peça registrada"]
  E3 --> P2["PO Atualizar quantidade do saldo físico do estoque"] --> C4["CMD Atualizar saldo físico do item"] --> E4["EV Saldo físico das peças atualizado"]

  classDef cmd fill:#bfe0ff,stroke:#2b78c2,color:#000
  classDef ev fill:#ffd9a0,stroke:#c77d00,color:#000
  classDef po fill:#dcc8ff,stroke:#7b4fd1,color:#000
  classDef se fill:#ffb6dc,stroke:#c2417c,color:#000
  class C1,C2,C3,C4 cmd
  class E0,E1,E2,E3,E4 ev
  class P1,P2 po
  class S se
```

| # | Ator e modelo de leitura | Comando | Evento | Política associada | Sistema externo |
|---|---|---|---|---|---|
| 1 | n/a | Receber notificação de atualização da OS para "cancelada" | OS atualizada para "cancelada" | Antes: Receber notificação da OS | Sistema de gestão de ordens de serviço |
| 2 | Funcionário; ML Lista de peças vinculadas à OS | Atualiza peças recuperadas | Peças recuperadas e retornadas ao estoque | n/a | n/a |
| 3 | Funcionário; ML Lista de peças vinculadas à OS | Atualiza peças não recuperadas | Perda ou avaria de peça registrada | n/a | n/a |
| 4 | n/a | Atualizar saldo físico do item | Saldo físico das peças atualizado | Antes: Atualizar quantidade do saldo físico do estoque | n/a |

No cancelamento, as peças vinculadas são separadas em recuperadas (voltam ao estoque) e não recuperadas (registradas como perda ou avaria), e o saldo físico é ajustado. Isso é coerente com o fluxo da OS, onde "finalizada" volta para "cancelada" quando o pagamento vence.

**7.3 Desvio: peça defeituosa e garantia**

Começa no evento "OS atualizada para em execução" (seta vermelha curta para baixo) e termina com uma seta vermelha longa que volta ao caminho principal.

```mermaid
flowchart TD
  E0["EV OS atualizada para em execução"] -.-> C1["CMD Registrar ocorrência de peça defeituosa"]
  P0["PO Receber notificação da OS"] --> C1
  C1 --> E1["EV Peça defeituosa identificada"]
  E1 --> C2["CMD Solicitar garantia junto ao fornecedor"] --> E2["EV Acionamento da garantia aceito pelo fornecedor"]
  S["SE Sistema do fornecedor de itens"] --> C2
  E2 --> C3["CMD Remover peça defeituosa do estoque"] --> E3["EV Retorno de peça defeituosa"]
  E3 --> C4["CMD Receber nova peça no estoque"] --> E4["EV Recebimento de nova peça funcional"]
  E4 -.->|"retorno ao fluxo principal"| P1["PO Receber notificação da OS (antes de finalizada)"]

  classDef cmd fill:#bfe0ff,stroke:#2b78c2,color:#000
  classDef ev fill:#ffd9a0,stroke:#c77d00,color:#000
  classDef po fill:#dcc8ff,stroke:#7b4fd1,color:#000
  classDef se fill:#ffb6dc,stroke:#c2417c,color:#000
  class C1,C2,C3,C4 cmd
  class E0,E1,E2,E3,E4 ev
  class P0,P1 po
  class S se
```

| # | Ator e modelo de leitura | Comando | Evento | Política associada | Sistema externo |
|---|---|---|---|---|---|
| 1 | Funcionário | Registrar ocorrência de peça defeituosa | Peça defeituosa identificada | Antes: Receber notificação da OS | n/a |
| 2 | Funcionário | Solicitar garantia junto ao fornecedor | Acionamento da garantia aceito pelo fornecedor | n/a | Sistema do fornecedor de itens |
| 3 | Funcionário; ML Lista de peças do estoque | Remover peça defeituosa do estoque | Retorno de peça defeituosa | n/a | n/a |
| 4 | Funcionário | Receber nova peça no estoque | Recebimento de nova peça funcional | n/a (a seta vermelha longa leva este evento de volta ao caminho principal) | n/a |

O fluxo é: a peça com defeito é registrada, a garantia é pedida ao fornecedor, a peça ruim sai do estoque, a nova chega ao estoque. Depois disso, o fluxo retorna ao ponto do caminho principal anterior à finalização da OS. O fluxo só mostra a garantia **aceita** pelo fornecedor; não há caminho para garantia recusada.

## 8. Agregados

O caminho principal agrupado em duas colunas do agregado ITEM. As exceções não entram nesta visão.

**ITEM (coluna principal, 12 linhas, de cima para baixo)**

| # | Comando | Evento | Política ou observação |
|---|---|---|---|
| 0 | (linha de entrada) | n/a | Receber notificação de atualização de itens na OS. SE Sistema de gestão de ordens de serviço |
| 1 | Adicionar itens a OS | Itens da OS solicitados ou atualizados | Efetuar a vinculação dos itens que foram solicitados pelo funcionário à OS |
| 2 | Vincular item à OS | Item vinculado a OS | Verificar o saldo físico dos itens subtraindo o saldo de itens reservados para verificar a disponibilidade |
| 3 | Verificar a disponibilidade dos itens vinculados à OS | Disponibilidade de itens da OS consultada | Recuperar o preço de venda registrado no sistema para envio |
| 4 | Enviar o preço de venda dos itens ao orçamento | Preço de venda dos itens vinculados à OS retornado ao orçamento | Receber notificação que o orçamento foi aprovado. SE Sistema de gestão de ordens de serviço |
| 5 | Aprova orçamento | Orçamento aprovado | Efetua a reserva dos itens que constam no orçamento aprovado |
| 6 | Atualiza o saldo reservado dos itens que foram reservados | Itens da OS reservados | Soma a quantidade dos novos itens que foram reservados no saldo reservado de cada item |
| 7 | Atualiza o saldo reservado dos itens que foram reservados | Saldo reservado do item atualizado | Receber notificação da OS. SE Sistema de gestão de ordens de serviço |
| 8 | Receber notificação de atualização da OS para "em execução" | OS atualizada para "em execução" | Receber notificação da OS |
| 9 | Receber notificação de atualização da OS para "finalizada" | OS atualizada para "finalizada" | Atualizar quantidade do saldo físico do estoque |
| 10 | Atualiza itens no estoque | Saldo físico do item atualizado | Receber notificação da OS. SE Sistema de gestão de ordens de serviço |
| 11 | Receber notificação de atualização da OS para "entregue" | OS atualizada para "entregue" | n/a |

**ITEM (segunda coluna, menor: entrada manual de estoque)**

Traz o ML Lista de itens do estoque e o ator Funcionário.

| Comando | Evento | Política ou observação |
|---|---|---|
| Adicionar itens ao estoque (Funcionário; ML Lista de itens do estoque) | Estoque atualizado | n/a |
| Preenchimento de dados dos itens (Funcionário) | Entrada manual de itens registrada | Atualizar quantidade do saldo físico do estoque |
| Atualiza itens no estoque | Saldo físico do item atualizado | n/a |

Esta coluna corresponde à entrada manual de estoque da fase 1.

## 9. Contexto

Um único contexto delimitado contém as duas colunas ITEM. O Sistema de gestão de ordens de serviço fica fora da fronteira e conversa com o estoque por notificações.

```mermaid
flowchart LR
  subgraph CTX["Gestão de Peças e Insumos"]
    A1["AG ITEM: fluxo principal"]
    A2["AG ITEM: entrada manual de estoque"]
  end
  OS["SE Sistema de gestão de ordens de serviço"] -->|"notificações: itens, orçamento, status da OS"| A1
  A1 -->|"preço, disponibilidade"| OS
  A1 <--> FOR["SE Sistema de fornecedor de itens"]

  classDef se fill:#ffb6dc,stroke:#c2417c,color:#000
  class OS,FOR se
```

| Elemento | Conteúdo |
|---|---|
| Contexto | Gestão de Peças e Insumos, com as duas colunas ITEM |
| Sistemas externos na fronteira | Sistema de gestão de ordens de serviço; Sistema de fornecedor de itens |

## 10. Relação com o fluxo da OS

Os dois fluxos descrevem o mesmo domínio por lados diferentes. O fluxo da OS (`fluxo-os.md`) chama o estoque de "Sistema de estoque de itens" e "Sistema de gestão de peças e insumos". Este fluxo chama a OS de "Sistema de gestão de ordens de serviço". A tabela liga as duas pontas.

| Momento | No fluxo da OS | No fluxo de estoque |
|---|---|---|
| Itens pedidos | Adiciona serviços, peças e insumos à OS (SE Sistema de estoque de itens) | PO Receber notificação de atualização de itens na OS, CMD Adicionar peças e insumos a OS |
| Disponibilidade e preço | Cálculo do orçamento (somatório do valor padrão dos serviços e dos valores de peças e insumos) | Verificar a disponibilidade, enviar o preço de venda ao orçamento |
| Decisão do cliente | Aprova, aprova parcialmente ou rejeita o orçamento | PO Receber notificação que o orçamento foi aprovado, aprovado parcialmente ou rejeitado |
| Serviço em execução, finalizada, entregue | Status da OS: em execução, finalizada, entregue | CMD Receber notificação de atualização da OS para cada status |
| Cancelamento por inadimplência | Desfaz o serviço e peças retornadas ao estoque (SE Sistema de gestão de peças e insumos) | Trilha de OS cancelada: peças recuperadas e não recuperadas |
| Problema no veículo | Volta para "em diagnóstico" | Registrar ocorrência de peça defeituosa e acionar garantia |
