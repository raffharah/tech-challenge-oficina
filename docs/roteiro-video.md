# Roteiro do vídeo — demonstração da API

Vídeo de cerca de 8 minutos mostrando só a API em funcionamento, acompanhando o ciclo de Ordens de Serviço (OS) de uma oficina. São 6 cenas em sequência: cada uma usa o que a anterior criou, então siga a ordem e não pule passos (os números de OS e os saldos esperados dependem disso).

| Cena | O que mostra | Tempo |
| --- | --- | --- |
| 1. Acesso | Autenticação JWT e catálogo pronto da oficina | 0:45 |
| 2. Chegada do cliente | Cadastro do cliente e do veículo, com validação de CPF e placa | 1:00 |
| 3. Troca de óleo | Ciclo completo da OS: abertura → diagnóstico → orçamento → aprovação → execução → entrega | 3:00 |
| 4. Falta de peça | Aprovação barrada por estoque, entrada de peça e nova aprovação | 1:30 |
| 5. Orçamento recusado | Cliente rejeita e a OS é cancelada | 0:45 |
| 6. Visão da oficina | Listagem, detalhamento e tempo médio de execução | 0:45 |

---

## Antes de gravar

- [ ] Estar na `main` atualizada (com a exigência de CPF na aprovação e rejeição já mergeada).
- [ ] Subir o ambiente do zero, para os IDs e saldos baterem com este roteiro:
  ```bash
  docker compose down -v
  docker compose up --build
  ```
- [ ] Abrir o Swagger: <http://localhost:8080/swagger-ui/index.html>
- [ ] Deixar este roteiro aberto em outra tela.
- [ ] Ensaiar uma vez, depois rodar `docker compose down -v && docker compose up --build` de novo antes da gravação valendo.

No Swagger, cada chamada é: abrir a rota → **Try it out** → colar o corpo → **Execute**.

### Dados da demo (cola)

| O quê | Valor |
| --- | --- |
| Usuário / senha | `funcionario` / `oficina123` |
| Cliente | Carlos Andrade, CPF `529.982.247-25` |
| Veículo | Volkswagen Gol 1.0 2019, placa `BRA2E19` (padrão Mercosul) |
| CPF de outra pessoa (para testar) | `111.444.777-35` |

Catálogo que o seed já cadastra (só os usados no vídeo):

| ID | Serviço | Mão de obra |
| --- | --- | --- |
| 1 | Troca de óleo do motor e filtro | R$ 90,00 |
| 2 | Substituição de pastilhas e fluido de freio dianteiro | R$ 180,00 |
| 3 | Substituição da correia dentada e bomba d'água | R$ 380,00 |

| ID | Item | Tipo | Preço | Saldo inicial |
| --- | --- | --- | --- | --- |
| 1 | Óleo de motor 5W-30 sintético, 1 L | INSUMO | R$ 54,90 | 60 |
| 2 | Filtro de óleo blindado | PECA | R$ 39,90 | 25 |
| 3 | Arruela do bujão do cárter | PECA | R$ 4,50 | 80 |
| 4 | Jogo de pastilhas de freio dianteiras | PECA | R$ 189,90 | 12 |
| 6 | Fluido de freio DOT 4, 500 ml | INSUMO | R$ 32,90 | 30 |
| 8 | Kit correia dentada com tensionador | PECA | R$ 349,90 | 6 |
| 9 | Bomba d'água | PECA | R$ 219,90 | 5 |

---

## Cena 1 — Acesso (0:45)

> Fala: "As rotas da oficina são protegidas por JWT. O funcionário faz login e recebe um token."

**1.1 Tentar sem token**

```http
GET /ordens-servico
```

Esperado: **401**.

**1.2 Login**

```http
POST /autenticacao
```
```json
{"usuario": "funcionario", "senha": "oficina123"}
```

Esperado: **200** com `token` e `expiraEm` (vale 60 minutos). Copie o token → botão **Authorize** no topo do Swagger → cole → **Authorize**.

**1.3 Catálogo pronto**

```http
GET /servicos
GET /itens
```

Esperado: **200**. 4 serviços e 16 peças e insumos já cadastrados.

> Fala: "A oficina já tem o catálogo de serviços e o estoque de peças e insumos cadastrados."

---

## Cena 2 — Chegada do cliente (1:00)

> Fala: "Chega um cliente novo. Primeiro cadastramos ele e o carro. O sistema valida CPF/CNPJ e placa."

**2.1 CPF inválido**

```http
POST /clientes
```
```json
{"nome": "Carlos Andrade", "cpfCnpj": "123.456.789-00"}
```

Esperado: **422** `CPF_CNPJ_INVALIDO`.

**2.2 CPF válido**

```http
POST /clientes
```
```json
{"nome": "Carlos Andrade", "cpfCnpj": "529.982.247-25"}
```

Esperado: **201**, `id: 1`, CPF salvo sem máscara (`52998224725`).

**2.3 Placa inválida**

```http
POST /veiculos
```
```json
{"placa": "AB-12345", "marca": "Volkswagen", "modelo": "Gol 1.0", "ano": 2019, "clienteId": 1}
```

Esperado: **422** `PLACA_INVALIDA`.

**2.4 Placa Mercosul**

```http
POST /veiculos
```
```json
{"placa": "BRA2E19", "marca": "Volkswagen", "modelo": "Gol 1.0", "ano": 2019, "clienteId": 1}
```

Esperado: **201**, `id: 1`.

---

## Cena 3 — Troca de óleo: ciclo completo da OS (3:00)

> Fala: "O Carlos trouxe o Gol para trocar o óleo. Vamos acompanhar a OS do começo ao fim."

**3.1 Abrir a OS** (identifica o cliente pelo CPF e o carro pela placa)

```http
POST /ordens-servico
```
```json
{"cpfCnpj": "529.982.247-25", "placa": "BRA2E19"}
```

Esperado: **201**, `id: 1`, status `RECEBIDA`. No `historico`: só `RECEBIDA`.

> Fala: "A OS nasce recebida: o carro chegou, mas ninguém avaliou ainda."

Opcional: tentar incluir um serviço agora (`POST /ordens-servico/1/servicos` com `{"servicoId": 1}`) responde **422** `ORDEM_FORA_DE_DIAGNOSTICO`.

**3.1.1 Mecânico inicia o diagnóstico**

```http
POST /ordens-servico/1/diagnostico
```

Esperado: **200**, status `EM_DIAGNOSTICO`. No `historico`: `RECEBIDA` → `EM_DIAGNOSTICO`, com horários diferentes.

> Fala: "Quando o mecânico começa a avaliar o carro, a OS passa para em diagnóstico."

**3.2 Incluir o serviço**

```http
POST /ordens-servico/1/servicos
```
```json
{"servicoId": 1}
```

Esperado: **201**, serviço "Troca de óleo do motor e filtro", R$ 90,00.

**3.3 Incluir peças e insumos** (três chamadas)

```http
POST /ordens-servico/1/itens
```
```json
{"itemId": 1, "quantidade": 4}
```
```json
{"itemId": 2, "quantidade": 1}
```
```json
{"itemId": 3, "quantidade": 1}
```

Esperado: **201** em cada uma. A OS lista 4 L de óleo, 1 filtro e 1 arruela.

**3.4 Gerar o orçamento**

```http
POST /ordens-servico/1/orcamento
```

Esperado: **200**, status `AGUARDANDO_APROVACAO`, `total: 354.00`.

> Fala: "O orçamento é calculado automaticamente: R$ 90 de mão de obra mais R$ 264 de peças e insumos."

**3.5 Tentar mudar a OS depois do orçamento**

```http
POST /ordens-servico/1/itens
```
```json
{"itemId": 3, "quantidade": 1}
```

Esperado: **422** `ORDEM_FORA_DE_DIAGNOSTICO`.

> Fala: "Depois do orçamento a OS fica travada, esperando o cliente."

**3.6 Cliente consulta a OS** (rota do cliente, sem token: ele informa o número da OS e o próprio CPF)

```http
GET /cliente/ordens-servico/1?cpfCnpj=529.982.247-25
```

Esperado: **200** com status `AGUARDANDO_APROVACAO`, itens e total.

**3.7 Alguém tenta aprovar com outro CPF**

```http
POST /cliente/ordens-servico/1/aprovacao
```
```json
{"cpfCnpj": "111.444.777-35"}
```

Esperado: **404** `ORDEM_SERVICO_NAO_ENCONTRADA`.

> Fala: "Só o titular decide. Para qualquer outra pessoa, a OS simplesmente não existe."

**3.8 Cliente aprova**

```http
POST /cliente/ordens-servico/1/aprovacao
```
```json
{"cpfCnpj": "529.982.247-25"}
```

Esperado: **200**, status `EM_EXECUCAO`.

**3.9 Estoque baixou sozinho**

```http
GET /itens/1
```

Esperado: `saldoFisico: 56` (eram 60). Filtro (`/itens/2`) foi de 25 para 24, arruela (`/itens/3`) de 80 para 79.

> Fala: "Na aprovação o sistema dá baixa nas peças, na mesma transação."

**3.10 Tentar entregar antes de terminar**

```http
POST /ordens-servico/1/entrega
```

Esperado: **422** `TRANSICAO_INVALIDA` ("de EM_EXECUCAO para ENTREGUE não permitida").

**3.11 Mecânico finaliza**

```http
POST /ordens-servico/1/finalizacao
```

Esperado: **200**, status `FINALIZADA`.

**3.12 Entrega do veículo**

```http
POST /ordens-servico/1/entrega
```

Esperado: **200**, status `ENTREGUE`.

**3.13 Cliente vê o histórico completo**

```http
GET /cliente/ordens-servico/1?cpfCnpj=52998224725
```

Esperado: **200**. O `historico` mostra os 6 status com data e hora: `RECEBIDA`, `EM_DIAGNOSTICO`, `AGUARDANDO_APROVACAO`, `EM_EXECUCAO`, `FINALIZADA`, `ENTREGUE`.

> Fala: "O cliente acompanha cada mudança de status em tempo real."

---

## Cena 4 — Falta de peça no estoque (1:30)

> Fala: "O mesmo carro volta para trocar a correia dentada. O mecânico pede 6 bombas d'água, mas só temos 5 em estoque."

**4.1 Abrir a OS 2, iniciar o diagnóstico, incluir serviço e peças**

```http
POST /ordens-servico
```
```json
{"cpfCnpj": "529.982.247-25", "placa": "BRA2E19"}
```

```http
POST /ordens-servico/2/diagnostico
```

```http
POST /ordens-servico/2/servicos
```
```json
{"servicoId": 3}
```

```http
POST /ordens-servico/2/itens
```
```json
{"itemId": 8, "quantidade": 1}
```
```json
{"itemId": 9, "quantidade": 6}
```

Esperado: **201** na abertura e nas inclusões, **200** no diagnóstico, OS `id: 2`.

**4.2 Orçamento**

```http
POST /ordens-servico/2/orcamento
```

Esperado: **200**, `total: 2049.30`.

**4.3 Cliente aprova, mas falta peça**

```http
POST /cliente/ordens-servico/2/aprovacao
```
```json
{"cpfCnpj": "529.982.247-25"}
```

Esperado: **422** `SALDO_INSUFICIENTE`, com `detalhes: [{"sku": "PEC-BOMBA-DAGUA", "quantidadePedida": 6, "saldoFisico": 5}]`.

> Fala: "A aprovação é tudo ou nada: nenhuma peça foi baixada e a OS continua aguardando aprovação."

**4.4 Chega mercadoria: entrada de estoque**

```http
POST /itens/9/entradas
```
```json
{"quantidade": 5}
```

Esperado: **200**, `saldoFisico: 10`.

**4.5 Cliente aprova de novo**

```http
POST /cliente/ordens-servico/2/aprovacao
```
```json
{"cpfCnpj": "529.982.247-25"}
```

Esperado: **200**, status `EM_EXECUCAO`. Em `GET /itens/9`, saldo `4`.

---

## Cena 5 — Orçamento recusado (0:45)

> Fala: "Outro dia, o Carlos pede orçamento dos freios e decide não fazer."

**5.1 Abrir a OS 3, iniciar o diagnóstico e montar o orçamento**

```http
POST /ordens-servico
```
```json
{"cpfCnpj": "529.982.247-25", "placa": "BRA2E19"}
```

```http
POST /ordens-servico/3/diagnostico
```

```http
POST /ordens-servico/3/servicos
```
```json
{"servicoId": 2}
```

```http
POST /ordens-servico/3/itens
```
```json
{"itemId": 4, "quantidade": 1}
```
```json
{"itemId": 6, "quantidade": 1}
```

```http
POST /ordens-servico/3/orcamento
```

Esperado: **200**, `total: 402.80`.

**5.2 Cliente rejeita**

```http
POST /cliente/ordens-servico/3/rejeicao
```
```json
{"cpfCnpj": "529.982.247-25"}
```

Esperado: **200**, status `CANCELADA`. Em `GET /itens/4`, o saldo continua `12`: nada saiu do estoque.

**5.3 OS cancelada não aceita mudanças**

```http
POST /ordens-servico/3/servicos
```
```json
{"servicoId": 1}
```

Esperado: **422** `ORDEM_FORA_DE_DIAGNOSTICO`.

---

## Cena 6 — Visão da oficina (0:45)

> Fala: "Para a gestão, a oficina lista as OS, vê o detalhe de cada uma e acompanha o tempo médio de execução."

**6.1 Listagem**

```http
GET /ordens-servico
```

Esperado: **200**, 3 OS, da mais recente para a mais antiga:

| OS | Status | Total |
| --- | --- | --- |
| 3 | `CANCELADA` | R$ 402,80 |
| 2 | `EM_EXECUCAO` | R$ 2.049,30 |
| 1 | `ENTREGUE` | R$ 354,00 |

**6.2 Detalhamento**

```http
GET /ordens-servico/2
```

Esperado: **200** com serviços, itens, total e histórico de status.

**6.3 Tempo médio de execução**

```http
GET /ordens-servico/tempo-medio-execucao
```

Esperado: **200** com `mediaMinutos` e `quantidade: 1`. Só entra a OS 1, a única que já passou por execução e foi finalizada. O valor é o tempo entre a aprovação e a finalização dela na gravação.

> Fala: "O tempo médio é calculado pelo histórico de status: de EM_EXECUCAO até FINALIZADA."

---

## Se algo der errado

| Sintoma | Causa provável | O que fazer |
| --- | --- | --- |
| 401 em rota da oficina | Token não colado ou expirado (60 min) | Repetir 1.2 e **Authorize** |
| OS ou cliente com `id` diferente do roteiro | Banco com dados de um ensaio | `docker compose down -v && docker compose up --build` |
| 409 ao cadastrar cliente ou veículo | Já cadastrado num ensaio | Mesmo que acima |
| 400 na aprovação ou rejeição | Faltou o corpo com `cpfCnpj` | Colar `{"cpfCnpj": "529.982.247-25"}` |

## Pelo terminal (opcional)

Se preferir `curl` ao Swagger:

```bash
TOKEN=$(curl -s -X POST localhost:8080/autenticacao -H 'Content-Type: application/json' \
  -d '{"usuario":"funcionario","senha":"oficina123"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')

curl -s -X POST localhost:8080/ordens-servico \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"cpfCnpj":"529.982.247-25","placa":"BRA2E19"}'
```

As rotas `/cliente/...` não levam o cabeçalho `Authorization`.
