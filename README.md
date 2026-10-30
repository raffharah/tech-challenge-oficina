# Sistema de Gestão de Oficina Mecânica

Back-end MVP para apoiar o atendimento e a execução de serviços de uma oficina mecânica, desenvolvido como Tech Challenge da Fase 1 do curso SOAT — Software Architecture, da FIAP.

## Objetivo

Organizar o atendimento da oficina e o ciclo de vida das Ordens de Serviço (OS), reduzindo falhas no controle de clientes, veículos, serviços, peças e insumos. A solução permite acompanhar o andamento do trabalho, gerar e obter aprovação de orçamentos e apoiar a gestão administrativa com validações e autenticação.

## Solução

Back-end monolítico modular com API REST. O MVP implementa três contextos do domínio:

- **Gestão de Clientes e Veículos (`cliente`):** cadastro de clientes identificados por CPF/CNPJ validado e de veículos identificados por placa (padrão antigo e Mercosul).
- **Gestão de Ordens de Serviço (`ordemservico`):** catálogo de serviços, abertura da OS, inclusão de serviços, peças e insumos, orçamento automático, aprovação ou rejeição pelo cliente, execução, finalização, entrega, acompanhamento e tempo médio de execução.
- **Gestão de Peças e Insumos (`estoque`):** cadastro de itens, entrada manual e baixa de saldo físico.

O contexto **Cobrança e Faturamento (`cobranca`)** está mapeado no DDD, mas fica fora do MVP (pacotes reservados, sem implementação). O escopo e as decisões que o recortam estão em [`.specs/ROADMAP.md`](.specs/ROADMAP.md) e [`.specs/STATE.md`](.specs/STATE.md).

### Fluxo da Ordem de Serviço

```text
RECEBIDA → EM_DIAGNOSTICO → AGUARDANDO_APROVACAO → EM_EXECUCAO → FINALIZADA → ENTREGUE
                                      └──────────→ CANCELADA (orçamento rejeitado)
```

| Ação | Endpoint | Status resultante |
| --- | --- | --- |
| Abrir OS com CPF/CNPJ e placa | `POST /ordens-servico` | `RECEBIDA` → `EM_DIAGNOSTICO` |
| Incluir/remover serviços e itens | `POST`/`DELETE /ordens-servico/{id}/servicos` e `/itens` | `EM_DIAGNOSTICO` |
| Gerar orçamento (serviços + itens) | `POST /ordens-servico/{id}/orcamento` | `AGUARDANDO_APROVACAO` |
| Cliente aprova (baixa o estoque, tudo ou nada) | `POST /cliente/ordens-servico/{id}/aprovacao` | `EM_EXECUCAO` |
| Cliente rejeita | `POST /cliente/ordens-servico/{id}/rejeicao` | `CANCELADA` |
| Finalizar os serviços | `POST /ordens-servico/{id}/finalizacao` | `FINALIZADA` |
| Entregar o veículo | `POST /ordens-servico/{id}/entrega` | `ENTREGUE` |

As transições são validadas no agregado `OrdemServico`; qualquer transição fora dessa sequência responde `422 TRANSICAO_INVALIDA`. Na aprovação, se algum item não tiver saldo físico suficiente, nada é baixado, a OS permanece em `AGUARDANDO_APROVACAO` e a resposta `422 SALDO_INSUFICIENTE` lista os itens em falta.

## Arquitetura

Monólito modular com DDD e Clean Architecture. Um único deployable; os contextos ficam isolados por subpacote dentro de cada camada, e a dependência segue `infrastructure` → `application` → `domain`:

- **`domain`:** Java puro. Entidades, objetos de valor (`CpfCnpj`, `Placa`), regras de negócio, exceções e interfaces (repositórios e portas).
- **`application`:** Java puro. Um caso de uso por classe (`AbrirOrdemServico`, `AprovarOrcamento`, `BaixarItens`), com método `executar`, recebendo e devolvendo DTOs.
- **`infrastructure`:** Spring e tecnologia. `http/` (controllers, requests validados, tratamento de erros), `db/` (entidades JPA e implementações dos repositórios), `gateway/` (implementações das portas entre contextos) e `config/` (beans, segurança e OpenAPI).

Um contexto usa outro apenas por uma porta declarada no próprio domínio: a OS consulta e baixa itens pela porta `EstoqueDeItens`, implementada em `infrastructure/gateway/estoque`. A transação abre no controller (`@Transactional`), de modo que aprovar o orçamento e baixar o estoque são atômicos. Essas regras são verificadas por testes ArchUnit ([`ArquiteturaTest`](src/test/java/br/com/fiap/oficina/ArquiteturaTest.java)).

Diagramas C4 em [`docs/ddd/c4/`](docs/ddd/c4/) e fluxos de negócio em [`docs/ddd/event-storming/`](docs/ddd/event-storming/).

## Tecnologias

- Java 21 e Spring Boot 4.1.2;
- Maven (com Maven Wrapper);
- Spring MVC, Bean Validation e Spring Data JPA;
- PostgreSQL 16 e Flyway;
- Spring Security OAuth2 Resource Server (JWT);
- Springdoc OpenAPI (Swagger UI);
- JUnit 5, AssertJ, Spring Boot Test, Testcontainers e ArchUnit;
- JaCoCo para cobertura;
- Docker e Docker Compose.

## Pré-requisitos

- Docker com Docker Compose v2 (suficiente para subir o ambiente completo);
- JDK 21 para executar fora do Docker ou rodar os testes (os testes de integração também exigem Docker em execução);
- Acesso à internet na primeira execução para baixar imagens, o Maven e as dependências.

## Como executar com Docker Compose

```bash
docker compose up --build
```

O Compose sobe, nesta ordem:

| Serviço | Imagem | Papel |
| --- | --- | --- |
| `postgres` | `postgres:16-alpine` | Banco de dados, com volume `postgres-data` |
| `migration` | `flyway/flyway:12.4.0-alpine` | Aplica as migrations de `src/main/resources/db/migration` e encerra |
| `seed` | `postgres:16-alpine` | Executa `docker/seed/seed.sql` (catálogo da oficina) e encerra |
| `app` | build do `Dockerfile` | API na porta 8080, iniciada após o seed; o Flyway da aplicação fica desligado, pois o schema é do serviço `migration` |

O seed é idempotente: ao subir de novo, itens e serviços já existentes não são alterados, preservando o saldo movimentado pelo uso. Para recriar o banco do zero, use `docker compose down -v`.

Variáveis opcionais (com padrão para demonstração): `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `APP_PORT` e `JWT_SEGREDO`.

### Catálogo inicial

O seed cadastra 16 itens de estoque (peças e insumos) e 4 serviços. O valor padrão do serviço corresponde à mão de obra; peças e insumos são incluídos na OS separadamente. O kit sugerido de cada serviço está abaixo e comentado em `docker/seed/seed.sql`:

| Serviço | Mão de obra | Kit sugerido |
| --- | --- | --- |
| Troca de óleo do motor e filtro | R$ 90,00 | `INS-OLEO-5W30-1L` ×4, `PEC-FILTRO-OLEO-3/4UNF` ×1, `PEC-ARRUELA-BUJAO-M14` ×1 |
| Substituição de pastilhas e fluido de freio dianteiro | R$ 180,00 | `PEC-PASTILHA-FREIO-DIANT` ×1, `INS-FLUIDO-FREIO-DOT4-500ML` ×1, `INS-LIMPA-FREIO-300ML` ×1 (discos `PEC-DISCO-FREIO-DIANT` ×2 se abaixo da espessura mínima) |
| Substituição da correia dentada e bomba d'água | R$ 380,00 | `PEC-KIT-CORREIA-DENTADA` ×1, `PEC-BOMBA-DAGUA` ×1, `INS-ADITIVO-RADIADOR-1L` ×2 |
| Revisão do sistema de ignição e admissão | R$ 150,00 | `PEC-VELA-IGNICAO-IRIDIO` ×4, `PEC-CABO-VELA-JOGO` ×1, `PEC-FILTRO-AR-MOTOR` ×1, `INS-LIMPA-TBI-300ML` ×1 |

Também há insumos de uso geral: `INS-GRAXA-LITIO-500G` e `INS-DESENGRIPANTE-300ML`. Clientes e veículos não fazem parte do seed e são cadastrados pela API.

## Como executar sem Docker

Com um PostgreSQL acessível em `localhost:5432/oficina` (ou configurado via `DB_URL`, `DB_USER` e `DB_PASSWORD`), a aplicação aplica as migrations ao iniciar:

```bash
./mvnw spring-boot:run
```

No Windows, use:

```powershell
.\mvnw.cmd spring-boot:run
```

Também é possível empacotar a aplicação com `./mvnw package` e executar o JAR gerado em `target/`. Nesse modo o seed não é aplicado; rode `docker/seed/seed.sql` com `psql` se quiser o catálogo inicial.

## Testes

```bash
# Unitários (*Test) e de arquitetura (ArchUnit)
./mvnw test

# Unitários + integração (*IT, Testcontainers) + verificação de cobertura
./mvnw verify
```

| Tipo | Sufixo | Onde | Executa em |
| --- | --- | --- | --- |
| Unitário | `*Test` | `domain` e `application`, sem Spring nem banco (repositórios em memória) | `./mvnw test` |
| Arquitetura | `*Test` | `ArquiteturaTest`, regras de camadas e isolamento de contextos | `./mvnw test` |
| Integração | `*IT` | `infrastructure`, API + PostgreSQL real via Testcontainers | `./mvnw verify` |

Os testes de integração sobem um PostgreSQL com Testcontainers e exigem Docker em execução. O JaCoCo gera o relatório em `target/site/jacoco/index.html`, e `./mvnw verify` falha se algum pacote de `domain` ou `application` ficar abaixo de 80% de cobertura de linhas.

## API e autenticação

Com a aplicação em execução:

- Swagger UI: <http://localhost:8080/swagger-ui/index.html>
- Especificação OpenAPI: <http://localhost:8080/v3/api-docs>

### Obter o token

As APIs administrativas exigem JWT. O usuário de demonstração é **`funcionario`**, senha **`oficina123`** (configuráveis por `OFICINA_USUARIO` e `OFICINA_SENHA_HASH`, este último um hash BCrypt). O token vale 60 minutos.

```bash
curl -X POST http://localhost:8080/autenticacao \
  -H 'Content-Type: application/json' \
  -d '{"usuario":"funcionario","senha":"oficina123"}'
```

Envie o token retornado no cabeçalho `Authorization: Bearer <token>`. No Swagger UI, chame `POST /autenticacao`, clique em **Authorize** e cole o token.

> As credenciais e o segredo JWT padrão (`JWT_SEGREDO`) existem apenas para demonstração local. Em qualquer outro ambiente, sobrescreva-os por variáveis de ambiente.

### Rotas

| Acesso | Rotas |
| --- | --- |
| Público | `POST /autenticacao`, Swagger/OpenAPI |
| Público (cliente da oficina) | `GET /cliente/ordens-servico/{id}?cpfCnpj=` (andamento da OS; exige o CPF/CNPJ do titular), `POST /cliente/ordens-servico/{id}/aprovacao`, `POST /cliente/ordens-servico/{id}/rejeicao` |
| JWT | `/clientes`, `/veiculos`, `/servicos`, `/itens` (CRUD e entradas de estoque), `/ordens-servico` (abertura, componentes, orçamento, finalização, entrega, listagem, detalhamento e `GET /ordens-servico/tempo-medio-execucao`) |

O cliente final não é autenticado no MVP (decisão AD-001 em [`.specs/STATE.md`](.specs/STATE.md)).

### Erros

Todas as respostas de erro seguem o formato `{"codigo", "mensagem", "campos", "detalhes"}`:

| Status | Quando |
| --- | --- |
| 400 | Requisição inválida: campo obrigatório, JSON malformado, parâmetro de tipo inválido |
| 401 | Token ausente ou inválido |
| 404 | Recurso ou rota inexistente |
| 405 / 415 | Método HTTP ou tipo de conteúdo não suportado |
| 409 | Conflito: CPF/CNPJ, placa, SKU ou nome de serviço duplicado; remoção de registro em uso |
| 422 | Regra de negócio: CPF/CNPJ ou placa inválidos, transição de status inválida, saldo insuficiente |

## Banco de dados

O banco escolhido é o **PostgreSQL 16**, pelos seguintes motivos:

- **Modelo relacional:** cliente, veículo, OS, serviços e itens de estoque são entidades fortemente relacionadas; chaves estrangeiras impedem, por exemplo, remover um serviço ou item já usado em uma OS.
- **Integridade no próprio banco:** índices únicos sem diferenciar maiúsculas (`lower(sku)`, `lower(nome)`), CPF/CNPJ e placa únicos e `check` de saldo físico não negativo e preços maiores que zero.
- **Transações ACID:** a aprovação do orçamento altera a OS e baixa o estoque de vários itens na mesma transação, tudo ou nada. O item de estoque tem controle de concorrência otimista (`versao`), evitando baixas simultâneas inconsistentes.
- **Evolução versionada:** o schema muda só por migrations Flyway (`src/main/resources/db/migration`), reproduzíveis em qualquer ambiente.
- **Mesmo motor em testes e execução:** os testes de integração usam PostgreSQL real via Testcontainers.
- **Maduro e open source**, com imagem oficial e suporte nativo do Spring Data JPA.

O banco local se chama `oficina` (usuário e senha `oficina`), com schema `public`, todos configuráveis por variáveis de ambiente.

## Estrutura do projeto

```text
.
├── Dockerfile                       # build multi-stage (JDK 21 → JRE 21)
├── docker-compose.yml               # postgres + migration + seed + app
├── docker/seed/seed.sql             # catálogo inicial da oficina
├── docs/
│   ├── fase1-tech-challenge.md      # enunciado
│   └── ddd/
│       ├── linguagem-ubiqua.md
│       ├── c4/                      # modelos de contexto e de componentes
│       └── event-storming/          # fluxo da OS e de peças e insumos
├── .specs/                          # roadmap, decisões (STATE) e specs por feature
├── src/
│   ├── main/java/br/com/fiap/oficina/
│   │   ├── domain/                  # cliente, ordemservico, estoque (cobranca reservado)
│   │   ├── application/             # casos de uso por contexto
│   │   └── infrastructure/          # http, db, gateway, config
│   ├── main/resources/
│   │   ├── application.yml
│   │   └── db/migration/            # V1..V4
│   └── test/java/br/com/fiap/oficina/   # espelha main; *Test unitários, *IT integração
├── mvnw / mvnw.cmd
└── pom.xml
```

## Documentação DDD

- [Linguagem Ubíqua e decisões do MVP](docs/ddd/linguagem-ubiqua.md)
- [Event Storming — fluxo de Ordem de Serviço](https://www.figma.com/board/tUNm5eJIG34rxl3cYE9nFQ/Event-storming-18SOAT?node-id=0-1&t=hixmrFlZPOJZbcMW-1)
- [Event Storming — fluxo de peças e insumos](https://www.figma.com/board/tUNm5eJIG34rxl3cYE9nFQ/Event-storming-18SOAT?node-id=2330-5454&t=wrvbbnXQTibMaU8n-1)
- [Modelo C4](https://www.figma.com/board/tUNm5eJIG34rxl3cYE9nFQ/Event-storming-18SOAT?node-id=2405-7907&t=wrvbbnXQTibMaU8n-1)

As versões exportadas dos diagramas e os arquivos Markdown de DDD estão em [`docs/ddd/`](docs/ddd/).

## Evidências da entrega

**Grupo 96**

### Integrantes
| Nome | RM | username |
|------|----|----------|
| Emigdio Bertolo Rizardi | rm377323 | emigdioriz |
| Vitória da Silva Berber | rm378173 | Vitória Berber - RM378173 |
| Matheus Luís Webber | rm378161 | Matheus Webber - rm378161 |
| Rafael Massakazo Hara | rm378138 | Hara |
| Raiana rocha oliveira | rm378287 | Rana |

### Entregáveis

- [Repositório no GitHub](https://github.com/raffharah/tech-challenge-oficina), branch `main`.
- **Pendente:** hash do commit da versão avaliada.
- **Pendente:** relatório da análise de vulnerabilidades.
- **Pendente:** link público do vídeo da entrega no YouTube.
