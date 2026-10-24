# AGENTS.md

Back-end MVP de uma oficina mecânica (Tech Challenge FIAP SOAT, fase 1): ordens de serviço, clientes, veículos, cobrança e estoque de peças e insumos.

## Fontes de verdade

- Requisitos e entregáveis: `docs/fase1-tech-challenge.md`.
- Nomes de classes, métodos, status e regras: `docs/ddd/linguagem-ubiqua.md`. A seção **Decisões do MVP** fecha o escopo. Registre ali um conceito novo antes de implementá-lo.
- Fluxos (comandos, eventos, políticas) por contexto: `docs/ddd/event-storming/fluxo-os.md` e `docs/ddd/event-storming/fluxo-pecas-insumos.md`. Consulte o fluxo do contexto que a tarefa toca.
- Sistemas e componentes (C4): `docs/ddd/c4/`.

## Stack

Java 21, Spring Boot 4.1.2, Maven, PostgreSQL, JWT nas APIs administrativas, OpenAPI (Swagger), Docker e docker-compose. Execução local; serviços AWS entram depois, trocando implementações de `infrastructure/`.

Pacote base: `br.com.fiap.oficina`.

## Arquitetura

Monolito modular com Clean Architecture e DDD. Um único deployable; os contextos ficam isolados por pacote dentro de cada camada.

Contextos (subpacotes):

| Subpacote | Contexto da linguagem ubíqua |
| --- | --- |
| `cliente` | Gestão de Clientes e Veículos |
| `ordemservico` | Gestão de Ordens de Serviço |
| `cobranca` | Cobrança e Faturamento |
| `estoque` | Gestão de Peças e Insumos |

Camadas e direção de dependência: `infrastructure` → `application` → `domain`.

- **domain**: Java puro. Entidades, objetos de valor, regras, exceções de negócio e interfaces (repositórios e portas para fora). A regra de negócio mora aqui: transição de status dentro de `OrdemServico`, validação de CPF/CNPJ e placa no construtor do objeto de valor, saldo disponível em `ItemEstoque`.
- **application**: Java puro. Um caso de uso por classe, nomeado pelo verbo da linguagem ubíqua (`CriarOrdemServico`, `AprovarOrcamento`), com método `executar`. Recebe e devolve DTOs (`CriarOrdemServicoDTO`). Orquestra domínio e repositórios; a regra fica no domínio.
- **infrastructure**: tudo que é Spring e tecnologia.
  - `http/`: `@RestController`, requests com `@Valid` (validação sintática), conversão request → DTO, `@RestControllerAdvice`.
  - `db/`: `@Entity`, Spring Data, `*RepositoryImpl` implementando o repositório do domínio e convertendo modelo ↔ entity.
  - `gateway/`: implementações das portas do domínio (e-mail, pagamento, nota fiscal, estoque).
  - `config/`: `BeanConfig` registra os casos de uso como beans; segurança JWT; agendamentos.

Regras de isolamento:

- Um contexto usa outro contexto só por uma porta declarada no próprio `domain`. Exemplo: `domain/ordemservico/EstoqueDeItens` é implementada em `infrastructure/gateway/estoque`, que chama os casos de uso de `application/estoque`.
- A transação abre na entrada (método do controller ou do job agendado, com `@Transactional`), mantendo `application` livre de Spring.
- Sistemas externos (e-mail, pagamento, nota fiscal) são portas com implementação simulada: a chamada monta o payload real e devolve uma resposta simulada, ambos expostos pela API. A implementação real substitui a simulada sem tocar em `domain` ou `application`.

## Estrutura

```
oficina-mecanica/
├── AGENTS.md
├── README.md
├── pom.xml
├── Dockerfile
├── docker-compose.yml                      # aplicação + PostgreSQL
├── docs/
│   ├── fase1-tech-challenge.md
│   └── ddd/
│       ├── linguagem-ubiqua.md
│       ├── event-storming/                  # fluxo-os, fluxo-pecas-insumos (.md, .png, .pdf)
│       └── c4/                              # modelo de contexto e de componentes
└── src/
    ├── main/java/br/com/fiap/oficina/
    │   ├── OficinaApplication.java          # @SpringBootApplication
    │   ├── domain/
    │   │   ├── DomainException.java         # violação de regra de negócio
    │   │   ├── cliente/
    │   │   │   ├── Cliente.java
    │   │   │   ├── Veiculo.java
    │   │   │   ├── CpfCnpj.java             # objeto de valor, valida dígitos
    │   │   │   ├── Placa.java               # objeto de valor, padrão antigo e Mercosul
    │   │   │   └── ClienteRepository.java
    │   │   ├── ordemservico/
    │   │   │   ├── OrdemServico.java        # raiz: status, orçamento, serviços, entrega
    │   │   │   ├── StatusOrdemServico.java
    │   │   │   ├── Orcamento.java
    │   │   │   ├── OrdemServicoRepository.java
    │   │   │   └── EstoqueDeItens.java      # porta para o contexto estoque
    │   │   ├── cobranca/
    │   │   │   ├── Pagamento.java
    │   │   │   ├── NotaFiscal.java
    │   │   │   └── SistemaDePagamentos.java # porta para sistema externo
    │   │   └── estoque/
    │   │       ├── ItemEstoque.java         # saldo físico, reservado, mínimo
    │   │       ├── Reserva.java
    │   │       └── ItemEstoqueRepository.java
    │   ├── application/
    │   │   ├── ordemservico/
    │   │   │   ├── CriarOrdemServico.java   # caso de uso
    │   │   │   ├── CriarOrdemServicoDTO.java
    │   │   │   └── OrdemServicoDTO.java
    │   │   └── estoque/
    │   │       └── ReservarItens.java
    │   └── infrastructure/
    │       ├── http/
    │       │   ├── ErroHandler.java         # @RestControllerAdvice
    │       │   └── ordemservico/
    │       │       ├── OrdemServicoController.java
    │       │       └── CriarOrdemServicoRequest.java
    │       ├── db/
    │       │   └── ordemservico/
    │       │       ├── OrdemServicoEntity.java
    │       │       ├── OrdemServicoJpaRepository.java
    │       │       └── OrdemServicoRepositoryImpl.java
    │       ├── gateway/
    │       │   ├── estoque/EstoqueDeItensImpl.java
    │       │   └── pagamento/SistemaDePagamentosSimulado.java
    │       └── config/
    │           ├── BeanConfig.java
    │           └── SecurityConfig.java
    ├── main/resources/
    │   ├── application.yml
    │   └── db/migration/                    # migrations versionadas
    └── test/java/br/com/fiap/oficina/       # espelha main
        ├── domain/                          # unitários, sem Spring
        ├── application/                     # unitários com repositórios em memória
        └── infrastructure/                  # integração (API + banco)
```

As classes listadas mostram o padrão de cada pasta; crie as demais seguindo o mesmo formato.

## Convenções

- Nomes de domínio em português, idênticos à linguagem ubíqua, sem acento (`Orcamento`, `SaldoDisponivel`). Sufixos técnicos em inglês: `Repository`, `Controller`, `Entity`, `Request`, `DTO`, `Impl`.
- Estado muda por métodos de negócio (`ordemServico.aprovarOrcamento()`), cada um validando a transição.
- Banco normalizado; o schema muda só por migration nova.

## Testes (TDD)

Todo código de produção nasce de um teste que falhou antes. Ciclo por comportamento: teste vermelho → código mínimo que passa → refatoração com os testes verdes. Uma entrega está pronta quando o comportamento novo ou alterado tem teste e `./mvnw verify` passa.

Os testes seguem F.I.R.S.T.:

- **Fast**: testes de `domain` e `application` são Java puro, sem Spring nem banco; repositórios e portas viram implementações em memória.
- **Independent**: cada teste monta os próprios dados e roda em qualquer ordem.
- **Repeatable**: mesmo resultado em qualquer máquina; data e hora entram por parâmetro ou `Clock`, banco real só via Testcontainers.
- **Self-validating**: o teste termina em asserção (AssertJ), aprovado ou reprovado sem inspeção manual.
- **Timely**: o teste é escrito antes do código que ele cobre.

Tipos e nomes:

| Tipo | Sufixo | Onde | Executa em |
| --- | --- | --- | --- |
| Unitário | `*Test` | `domain`, `application` | `./mvnw test` |
| Integração (API + PostgreSQL) | `*IT` | `infrastructure` | `./mvnw verify` |
| Arquitetura (ArchUnit) | `*Test` | raiz de `src/test` | `./mvnw test` |

Cobertura: JaCoCo mede todos os testes; `./mvnw verify` falha se algum pacote de `domain` ou `application` ficar abaixo de 80% de linhas. Relatório em `target/site/jacoco/index.html`. Os testes de integração exigem Docker em execução.
