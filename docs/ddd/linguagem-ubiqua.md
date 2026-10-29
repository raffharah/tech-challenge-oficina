## O que é Linguagem Ubíqua?

Linguagem Ubíqua (Ubiquitous Language) é um vocabulário compartilhado entre desenvolvedores e especialistas de domínio, usado de forma consistente no código, nos documentos e nas conversas do time.

O objetivo é eliminar ambiguidades e garantir que todos falem a mesma língua ao se referir aos conceitos do sistema.

---

## Contextos delimitados

- Gestão de Clientes e Veículos
- Gestão de Ordens de Serviço
- Cobrança e Faturamento
- Gestão de Peças e Insumos

## Glossário

### Gestão de Clientes e Veículos

| Termo | Definição |
| --- | --- |
| Cliente | Pessoa física ou jurídica que solicita a execução de um serviço em um de seus veículos. |
| Veículo | Bem do cliente identificado de forma única pela placa, que serve como objeto central para a execução de um serviço. |
| Funcionário (Recepcionista) | Pessoa responsável por identificar o cliente, validar as informações de cadastro e registrar a solicitação inicial de serviço no sistema. |
| Sistema | Plataforma automatizada que realiza as validações de regras de negócio (como checagem de CPF/CNPJ e validação do formato de placa). |
| Serviço | Atividade ou manutenção oferecida pela oficina e solicitada pelo cliente na etapa de recepção. |
| CPF/CNPJ | Identificador fiscal do cliente, único no sistema. É validado pelo formato e pelos dígitos verificadores, conforme as regras da Receita Federal. |
| Placa | Identificador único do veículo, validado pelo formato (padrão antigo ABC-1234 ou Mercosul ABC1D23). |
| Pedido de serviço | Solicitação inicial do cliente. Depois da identificação do cliente e do veículo, resulta na abertura da OS. |
| Cliente suspenso | Cliente impedido de abrir novos pedidos de serviço por inadimplência, consequência de uma OS cancelada por pagamento vencido. |

### Gestão de Ordens de Serviço

| Termo | Definição |
| --- | --- |
| Cliente | Proprietário do veículo (ou representante) que aprova o orçamento e autoriza a execução dos serviços. |
| Veículo | Objeto de trabalho da oficina, identificado de forma única pela placa, sujeito ao diagnóstico, manutenção e emissão de OS. |
| Funcionário (Mecânico/Técnico) | Profissional responsável por realizar o diagnóstico, registrar os serviços, peças e insumos necessários e executar a manutenção. |
| Sistema | Plataforma responsável por calcular o orçamento, atualizar os status do fluxo da OS e notificar as partes interessadas sobre alterações nos status. |
| OS - Ordem de Serviço | Entidade principal do contexto que agrupa o diagnóstico, os serviços, as peças, os insumos e o orçamento, controlando todo o ciclo de vida do trabalho na oficina. |
| Peça | Componente físico integrado ao veículo durante a manutenção que possui valor individual contabilizado no orçamento. |
| Insumo | Material consumido durante a execução do serviço (ex: óleo, fluidos) que não permanece como peça fixa, mas cujo custo integra o orçamento. |
| Orçamento | Cálculo consolidado do valor total da OS, composto pelo somatório dos serviços prestados e do custo das peças e insumos utilizados, aprovado pelo cliente antes da execução dos serviços. |
| Serviço | Conjunto de atividades e manutenções realizadas por um funcionário em um veículo. |
| Diagnóstico | Etapa em que o funcionário avalia o veículo para identificar os problemas e determinar a lista de serviços, peças e insumos necessários. |
| Recusa parcial | O cliente recusa só alguns componentes do orçamento, e a OS é renegociada. |
| Recusa total | O cliente recusa todo o orçamento, ou recusa novamente o orçamento renegociado. A OS é cancelada. |
| Renegociação | Uma única rodada após a recusa parcial: a OS volta a Em diagnóstico, os itens são ajustados e um novo orçamento é calculado e enviado. |
| Histórico de orçamentos | Registro de cada orçamento calculado para a OS. O orçamento anterior é a versão que se retoma quando um serviço adicional é rejeitado. |
| Prazo | Data limite para concluir os serviços, informada pelo funcionário ao calcular o orçamento. A OS é considerada atrasada quando o prazo passa sem a OS estar Finalizada. |
| Tempo médio de execução | Média do tempo entre o início e a conclusão dos serviços da OS. |
| Entrega do veículo | Entrega ao cliente após o pagamento. Muda a OS para Entregue. |
| Devolução do veículo | Retorno do veículo ao cliente quando a OS foi cancelada. Não muda o status. |
| Status da OS - “Recebida” | OS criada, veículo recebido, ainda sem diagnóstico. |
| Status da OS - “Em diagnóstico” | O mecânico avalia o veículo e define serviços, peças e insumos. Volta a esse status na renegociação e no serviço adicional. |
| Status da OS - “Aguardando aprovação” | Orçamento enviado, à espera da decisão do cliente. |
| Status da OS - “Em execução” | Orçamento aprovado e OS liberada. Os serviços são iniciados e concluídos um a um. |
| Status da OS - “Finalizada” | Todos os serviços concluídos. Segue para a cobrança. |
| Status da OS - “Entregue” | Veículo entregue ao cliente após o pagamento. |
| Status da OS - “Cancelada” | OS encerrada sem conclusão normal, por orçamento rejeitado ou pagamento vencido. Não tem saída. |

### Cobrança e Faturamento

| Termo | Definição |
| --- | --- |
| Cliente | Pessoa física ou jurídica que assumiu a responsabilidade financeira pelo pagamento dos valores acordados no orçamento aprovado. |
| Sistema Interno de Cobrança | Plataforma responsável por orquestrar o envio de notificações de cobrança, comunicar-se com os serviços externos (de pagamento e emissão de nota fiscal) e registrar o status financeiro na OS. |
| OS - Ordem de Serviço | A OS registra o pagamento; o estado da cobrança (a vencer, em atraso, vencido, pago) é do Pagamento. |
| Orçamento | Montante financeiro aprovado referente aos serviços, peças e insumos, que serve como valor base para a geração de cobranças e faturas. |
| Serviço | Unidade de trabalho finalizada cujo valor compõe o total faturável da Ordem de Serviço. |
| Pagamento | Ato de quitação financeira (integral ou parcelada) dos valores devidos, realizado pelo cliente e verificado pelo sistema de gestão de pagamentos para autorizar a liberação da OS e a emissão da Nota Fiscal. |
| Nota Fiscal (NF) | Documento fiscal emitido após o registro do pagamento efetuado, formalizando a prestação de serviços perante a Receita. |
| Sistema de Nota Fiscal Eletrônica | Serviço externo integrado encarregado de gerar, validar e emitir a Nota Fiscal para o cliente. |
| Sistema de Gestão de Pagamentos | Sistema externo (gateway/processador de pagamentos) responsável por processar a transação financeira do cliente e confirmar o recebimento do valor para a oficina. |
| Cobrança | Pedido de pagamento enviado ao cliente quando a OS é finalizada, com as informações para pagar. |
| Pagamento em atraso | Passaram 5 dias úteis da finalização da OS sem pagamento registrado. Cliente e funcionários são notificados. |
| Pagamento vencido | Passaram 5 dias úteis da notificação de atraso sem pagamento registrado. Gera o cancelamento da OS. |
| Cancelamento por inadimplência | A OS passa de Finalizada para Cancelada, o cliente é suspenso, as peças recuperadas voltam ao estoque e o veículo é devolvido. |
| Registro do pagamento na OS | Depois do pagamento efetuado, o sistema registra o pagamento na OS. Não altera o status. |

### Gestão de Peças e Insumos

| Termo | Definição |
| --- | --- |
| Funcionário | Quem cadastra itens, dá entrada, registra descarte e faz a reposição. |
| Sistema | Executa as políticas do Estoque (reservar, baixar saldo, alertar estoque mínimo) |
| OS - Ordem de Serviço | Entidade principal do contexto que agrupa o diagnóstico, os serviços, as peças, os insumos e o orçamento, controlando todo o ciclo de vida do trabalho na oficina. |
| Item de Estoque | Termo utilizado para identificar os itens que são armazenados em estoque, podendo ser classificados em peça ou insumo. |
| Peça | Componente físico integrado ao veículo durante a manutenção que possui valor individual contabilizado no orçamento. |
| Insumo | Material consumido durante a execução do serviço (ex: óleo, fluidos) que não permanece como peça fixa, mas cujo custo integra o orçamento. |
| Insumos Comprometidos | Insumos cujo prazo de validade foi ultrapassado ou cuja integridade física foi comprometida a ponto de impedir seu uso em um serviço. |
| Orçamento | Cálculo consolidado do valor total da OS, composto pelo somatório dos serviços prestados e do custo das peças e insumos utilizados, aprovado pelo cliente antes da execução dos serviços. |
| Serviço | Conjunto de atividades e manutenções realizadas por um funcionário em um veículo. |
| Reserva de Item | Identificação no sistema que um determinado item será utilizado em uma OS específica e não pode ser utilizado em outros. |
| Saldo Físico de Item | Quantidade de um item que está fisicamente disponível no estoque. |
| Saldo Reservado de Item | Soma das quantidades reservadas para OS aprovadas e ainda não consumidas. |
| Saldo Disponível | Quantidade do item que ainda pode ser reservada para novas OS. Calculado como **saldo físico − saldo reservado** (por exemplo, 10 − 2 = 8). Não é digitado e nunca fica negativo. |
| Estoque mínimo | Quantidade abaixo da qual o funcionário é notificado da necessidade de reposição. Compara com o **saldo físico**, verificado após cada consumo e descarte. |
| Entrada de estoque | Acréscimo ao saldo físico: saldo inicial, entrada manual ou recebimento de reposição. |
| Consumo | Baixa do saldo físico e liberação da reserva quando a OS é finalizada. |
| Descarte | Baixa do saldo físico de insumos comprometidos. |
| Item inativo | Item que não aceita novas reservas, mas mantém o histórico. |
| Disponibilidade | Verificação de que o saldo disponível cobre a quantidade pedida pela OS. O resultado é Disponibilidade confirmada ou Falta de estoque detectada. |
| Devolução ao estoque | Retorno das peças recuperadas de uma OS cancelada. Insumos consumidos não voltam, e peças não recuperadas são registradas como perda ou avaria. |
| Fornecedor | Quem fornece itens |
| Reposição | Pedido de novos itens ao fornecedor |
| Item fora de linha | Descontinuado, vai para inativo |
| Peça defeituosa | Peça com ocorrência registrada, sujeita a garantia |

---

## Decisões do MVP

| Tema | Decisão |
| --- | --- |
| Sistemas | OS e Estoque ("Sistema de estoque de itens" = "Sistema de gestão de peças e insumos") rodam no mesmo monolito, como módulos isolados. A OS fala com o Estoque só por uma porta (interface) do domínio. |
| Reserva | Acontece só na aprovação do orçamento: o sistema verifica de novo a disponibilidade e reserva. Falta de estoque na aprovação mantém a OS em Aguardando aprovação e notifica o funcionário. Recusa antes da aprovação não tem reserva para liberar. |
| Liberação da reserva | O consumo (OS Finalizada) baixa o saldo físico e libera a reserva. Serviço adicional rejeitado não reserva nada. |
| Estoque mínimo | Comparado com o saldo físico. Gera só a notificação ao funcionário. |
| Fornecedor | Reposição, orçamento de fornecedor e garantia ficam fora do MVP. A entrada de estoque é manual. |
| Prazo | Informado manualmente no orçamento. O atraso é verificado por rotina agendada diária. |
| Aguardando aprovação | Não expira no MVP. |
| Catálogo de serviços | Serviço tem nome único (sem diferenciar maiúsculas) e valor padrão maior que zero. A OS copia o valor padrão ao incluir o serviço; alterar o catálogo não muda OS existentes. Serviço referenciado por OS não é removido. |
| Itens de estoque | Item tem SKU único (sem diferenciar maiúsculas), tipo peça ou insumo, preço de venda maior que zero e saldo físico. O saldo muda só por entrada e baixa, nunca fica negativo. A baixa de vários itens é tudo ou nada. |
| Agregados do Estoque | Item de Estoque (saldo físico, saldo reservado, estoque mínimo, situação) e Reserva (itens reservados por OS). |
| Agregados da OS | Ordem de Serviço (inclui orçamento, serviços e entrega do veículo; "Finaliza Serviço" é a etapa de entrega da OS). Cobrança e Faturamento: Pagamento (cobrança e registro do pagamento) e Nota Fiscal. |
| Sistemas externos | E-mail, pagamento e nota fiscal são portas com implementação simulada. Cada chamada monta o payload real e devolve uma resposta simulada, ambos expostos pela API para acompanhar o processo. A implementação real (ex.: serviços AWS) substitui a simulada sem mudar o domínio. |

---

## Regras de uso

- Usar os termos exatamente como definidos aqui — tanto no código (classes, métodos, variáveis) quanto em reuniões e documentos.
- Ao surgir um novo conceito, registrá-lo aqui antes de implementar.
- Se um termo gerar confusão, revisitar a definição com o time.

---

## Histórico de alterações

| Data | Termo | Alteração | Responsável |
| --- | --- | --- | --- |
| — | — | — | — |