# Sistema de Gestão de Oficina Mecânica

Back-end MVP para apoiar o atendimento e a execução de serviços de uma oficina mecânica, desenvolvido como Tech Challenge da Fase 1 do curso SOAT — Software Architecture, da FIAP.

> **Estado deste esboço:** a documentação de requisitos, domínio e arquitetura está disponível em `docs/`, mas o código de aplicação ainda não foi implementado. No momento, `src/` contém apenas diretórios-placeholder (`.gitkeep`); portanto, os comandos de execução abaixo descrevem o uso previsto e ainda não iniciam uma API funcional. Complete os itens marcados com **Necessário detalhamento** quando as decisões e a implementação estiverem disponíveis.

## Objetivo

Organizar o atendimento da oficina e o ciclo de vida das Ordens de Serviço (OS), reduzindo falhas no controle de clientes, veículos, serviços, peças e insumos. A solução deve permitir acompanhar o andamento do trabalho, gerar e obter aprovação de orçamentos e apoiar a gestão administrativa com validações e autenticação.

## Solução

A proposta é um back-end monolítico modular com API REST. O domínio está dividido em quatro contextos:

- **Gestão de Clientes e Veículos:** cadastro e identificação de clientes por CPF/CNPJ e veículos por placa.
- **Gestão de Ordens de Serviço:** criação e acompanhamento da OS, diagnóstico, serviços, orçamento, aprovação, execução e entrega.
- **Cobrança e Faturamento:** cobrança, registro de pagamento e emissão de Nota Fiscal.
- **Gestão de Peças e Insumos:** cadastro de itens, entradas manuais, disponibilidade, reservas, consumo e descarte.

O fluxo principal previsto para uma OS é: recebida → em diagnóstico → aguardando aprovação → em execução → finalizada → entregue. O domínio também documenta cancelamento, recusas e renegociação do orçamento, serviço adicional, atraso, cobrança vencida e devolução de peças recuperadas. Os sistemas externos de e-mail, pagamento e nota fiscal têm implementações simuladas previstas para o MVP. A reposição por fornecedor está fora do escopo do MVP; a entrada de estoque é manual.

## Arquitetura

Monólito modular com DDD e Clean Architecture. Os contextos são organizados em pacotes por camada, com dependências direcionadas de `infrastructure` para `application` e de `application` para `domain`:

- **`domain`:** entidades, objetos de valor, regras de negócio, exceções e interfaces/portas.
- **`application`:** casos de uso, um por classe, que orquestram domínio e repositórios sem depender de Spring.
- **`infrastructure`:** integração com tecnologias, API HTTP, persistência, gateways e configuração.

Os contextos devem se comunicar por portas definidas no domínio. A aplicação está planejada para usar PostgreSQL e migrations versionadas. Consulte os diagramas C4 em [`docs/ddd/c4/`](docs/ddd/c4/) e os fluxos de negócio em [`docs/ddd/event-storming/`](docs/ddd/event-storming/).

## Tecnologias

Tecnologias declaradas no projeto:

- Java 21;
- Spring Boot 4.1.2;
- Maven (com Maven Wrapper);
- Spring MVC, Bean Validation e Spring Data JPA;
- PostgreSQL e Flyway;
- Spring Security OAuth2 Resource Server para autenticação/autorização com JWT;
- Springdoc OpenAPI para documentação Swagger;
- JUnit/Spring Boot Test, Testcontainers e ArchUnit;
- JaCoCo para relatório e verificação de cobertura.

Docker e Docker Compose fazem parte dos requisitos da entrega, mas ainda não há `Dockerfile` nem `docker-compose.yml` neste repositório.

## Pré-requisitos

- JDK 21;
- Docker em execução para os testes de integração com Testcontainers e, futuramente, para subir o PostgreSQL por Compose;
- Acesso à internet na primeira execução do Maven Wrapper para baixar o Maven e as dependências.

**Necessário detalhamento:** definir e documentar se a execução local exigirá PostgreSQL instalado ou se será fornecido por Docker Compose, além das versões mínimas de Docker/Docker Compose e das variáveis de ambiente necessárias.

## Como executar

O Maven Wrapper está incluído no repositório. Quando a classe principal, a configuração da aplicação e o banco estiverem implementados, a execução local prevista é:

```bash
./mvnw spring-boot:run
```

No Windows, use:

```powershell
.\mvnw.cmd spring-boot:run
```

Também é possível empacotar a aplicação com `./mvnw package` e executar o JAR gerado em `target/`.

**Necessário detalhamento:** estes comandos ainda não são executáveis como aplicação: faltam a classe principal Spring Boot, os componentes da aplicação, `application.yml` (ou configuração equivalente), migrations e as configurações para conectar ao PostgreSQL. Não há Dockerfile nem Compose para iniciar o ambiente completo.

## Testes

Com os testes implementados, use:

```bash
# Testes unitários e de arquitetura (quando implementados)
./mvnw test

# Verificações Maven e relatório de cobertura
./mvnw verify
```

Os testes unitários de domínio e aplicação devem ser rápidos e independentes de Spring e banco. Os testes de integração (`*IT`) estão planejados para usar Testcontainers e requerem Docker. O JaCoCo está configurado para gerar relatório em `target/site/jacoco/` e exigir cobertura mínima de 80% das linhas nos pacotes `domain` e `application`.

**Necessário detalhamento:** ainda não existem classes de teste; os diretórios de testes contêm apenas `.gitkeep`. O Failsafe está declarado no `pom.xml`, mas ainda não tem metas associadas às fases `integration-test` e `verify`; configurar esse vínculo para que `./mvnw verify` execute testes `*IT`. Registrar os resultados e a cobertura quando os fluxos forem implementados.

## API e autenticação

A dependência Springdoc OpenAPI está declarada. Quando a API estiver implementada e em execução, os endereços padrão previstos são:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Especificação OpenAPI: `http://localhost:8080/v3/api-docs`

As APIs administrativas deverão usar JWT. As dependências de segurança estão declaradas, mas a configuração de segurança, os endpoints protegidos e o fluxo de obtenção do token ainda não foram implementados.

**Necessário detalhamento:** informar o procedimento para obter um token e quais operações exigem autenticação. Não há usuários nem credenciais de demonstração definidos; não inclua segredos reais neste README.

## Banco de dados

O banco escolhido para a solução é o **PostgreSQL**, conforme a dependência JDBC, o driver, o suporte do Flyway e os diagramas C4 do projeto. É um banco relacional adequado ao modelo de entidades relacionadas (clientes, veículos, OS, itens e pagamentos), com constraints e transações para preservar integridade em operações como aprovar uma OS e reservar itens. O uso de migrations versionadas ajuda a manter a evolução do schema reproduzível entre ambientes.

**Necessário detalhamento:** validar e registrar a justificativa arquitetural final, além de documentar nome do banco/schema, credenciais locais fornecidas por variáveis de ambiente e o comando de inicialização/migration.

## Estrutura do projeto

```text
.
├── docs/
│   ├── fase1-tech-challenge.md
│   └── ddd/
│       ├── linguagem-ubiqua.md
│       ├── c4/
│       └── event-storming/
├── src/
│   ├── main/
│   │   ├── java/br/com/fiap/oficina/
│   │   │   ├── domain/
│   │   │   ├── application/
│   │   │   └── infrastructure/
│   │   └── resources/db/migration/
│   └── test/java/br/com/fiap/oficina/
├── mvnw
├── mvnw.cmd
└── pom.xml
```

Os contextos (`cliente`, `ordemservico`, `cobranca` e `estoque`) devem aparecer dentro das camadas `domain`, `application` e `infrastructure`. Em `infrastructure`, a organização prevista inclui `http/`, `db/`, `gateway/` e `config/`. Os diretórios listados em `src/` existem, mas ainda não contêm implementação.

## Documentação DDD

- [Linguagem Ubíqua e decisões do MVP](docs/ddd/linguagem-ubiqua.md)
- [Event Storming — fluxo de Ordem de Serviço](https://www.figma.com/board/tUNm5eJIG34rxl3cYE9nFQ/Event-storming-18SOAT?node-id=0-1&t=hixmrFlZPOJZbcMW-1)
- [Event Storming — fluxo de peças e insumos](https://www.figma.com/board/tUNm5eJIG34rxl3cYE9nFQ/Event-storming-18SOAT?node-id=2330-5454&t=wrvbbnXQTibMaU8n-1)
- [Modelo C4](https://www.figma.com/board/tUNm5eJIG34rxl3cYE9nFQ/Event-storming-18SOAT?node-id=2405-7907&t=wrvbbnXQTibMaU8n-1)

**Necessário detalhamento:** confirmar que os links externos estão acessíveis sem solicitação manual de permissão e correspondem à documentação exigida. As versões exportadas dos diagramas e os arquivos Markdown de DDD também estão disponíveis neste repositório, em [`docs/ddd/`](docs/ddd/).

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

[Repositório no GitHub](https://github.com/raffharah/tech-challenge-oficina)

Branch prevista: `main` — commit da versão avaliada: **Necessário detalhamento** (informar o hash final).

**Necessário detalhamento:** confirmar branch e hash do commit da versão avaliada e adicionar o resultado ou link do relatório de análise de vulnerabilidades.

**Necessário detalhamento:** adicionar o link público e clicável para o vídeo da entrega no YouTube. O vídeo deve apresentar a versão correspondente ao commit entregue e demonstrar a arquitetura, os fluxos principais, a API, os testes e a análise de vulnerabilidades.
