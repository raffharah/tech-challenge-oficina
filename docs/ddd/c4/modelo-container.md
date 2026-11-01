# Modelo C4 — Container

O sistema roda como um monólito Spring Boot sobre um PostgreSQL. O schema e o catálogo inicial são preparados por dois containers de execução única, antes da API subir (ver [`docker-compose.yml`](../../../docker-compose.yml)).

```mermaid
---
title: Container - Sistema de gestão da oficina mecânica
config:
  flowchart:
    curve: basis
    wrappingWidth: 240
    nodeSpacing: 60
    rankSpacing: 100
---
flowchart TB
    funcionario(["<b>Funcionário</b><br/>[Pessoa]"])
    cliente(["<b>Cliente</b><br/>[Pessoa]"])

    subgraph sistema["Sistema de gestão da oficina"]
        api["<b>API da oficina</b><br/>[Container: Java 21, Spring Boot 4.1]<br/>Monólito modular com a API REST e o Swagger"]
        migration["<b>Migration</b><br/>[Container: Flyway 12.4]<br/>Aplica as migrations versionadas e encerra"]
        seed["<b>Seed</b><br/>[Container: psql]<br/>Cadastra o catálogo da oficina e encerra"]
        bd[("<b>Banco de dados</b><br/>[Container: PostgreSQL 16]<br/>Clientes, veículos, serviços, itens de estoque e OS")]
    end

    funcionario -->|"Usa [JSON/HTTPS, JWT]"| api
    cliente -->|"Consulta e decide o orçamento [JSON/HTTPS]"| api
    api -->|"Lê e grava [JDBC/SQL]"| bd
    migration -->|"Cria e evolui o schema [JDBC/SQL]"| bd
    seed -->|"Insere serviços, peças e insumos [SQL]"| bd

    classDef pessoa fill:#08427b,stroke:#052e56,color:#fff
    classDef container fill:#438dd5,stroke:#2e6295,color:#fff
    classDef apoio fill:#85bbf0,stroke:#5d82a8,color:#000
    class funcionario,cliente pessoa
    class api,bd container
    class migration,seed apoio
    style sistema fill:#ffffff,stroke:#444,stroke-dasharray:6 4,color:#000
```

Ordem de subida: `postgres` → `migration` → `seed` → `api`. Cada etapa só começa quando a anterior termina com sucesso. Fora do Docker, a própria API aplica as migrations ao iniciar.
