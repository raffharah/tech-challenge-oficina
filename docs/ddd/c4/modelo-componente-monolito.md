# Modelo C4 — Componentes da API

A API é um monólito modular: cada contexto tem seus controllers, casos de uso e repositórios, e o contexto de Ordens de Serviço só acessa os outros dois por portas declaradas no próprio domínio (`CadastroDeVeiculos` e `EstoqueDeItens`). Essas regras de dependência são verificadas pelo [`ArquiteturaTest`](../../../src/test/java/br/com/fiap/oficina/ArquiteturaTest.java).

```mermaid
---
title: Componentes - API da oficina
config:
  flowchart:
    curve: basis
    wrappingWidth: 230
    nodeSpacing: 40
    rankSpacing: 80
---
flowchart TB
    funcionario(["<b>Funcionário</b><br/>[Pessoa]"])
    cliente(["<b>Cliente</b><br/>[Pessoa]"])

    subgraph api["API da oficina (Spring Boot)"]
        direction TB
        auth["<b>Autenticação</b><br/>[Componente: REST Controller]<br/>AutenticacaoController: emite o token"]
        seguranca["<b>Segurança</b><br/>[Componente: Spring Security]<br/>SecurityConfig: valida JWT nas rotas administrativas"]

        subgraph ctx_cliente["Clientes e Veículos"]
            direction TB
            ctrl_cliente["<b>Controllers de cadastro</b><br/>[Componente: REST Controller]<br/>ClienteController, VeiculoController"]
            uc_cliente["<b>Casos de uso de cliente</b><br/>[Componente: Java]<br/>CadastrarCliente, ConsultarVeiculos, ..."]
            repo_cliente["<b>Repositórios de cliente</b><br/>[Componente: Spring Data JPA]<br/>ClienteRepositoryImpl, VeiculoRepositoryImpl"]
        end

        subgraph ctx_os["Ordens de Serviço"]
            direction TB
            ctrl_os["<b>Controllers da oficina</b><br/>[Componente: REST Controller]<br/>OrdemServicoController, ServicoController"]
            ctrl_portal["<b>Controller do cliente</b><br/>[Componente: REST Controller]<br/>ClienteOrdemServicoController"]
            uc_os["<b>Casos de uso de OS</b><br/>[Componente: Java]<br/>AbrirOrdemServico, AprovarOrcamento, FinalizarOrdemServico, ..."]
            repo_os["<b>Repositórios de OS</b><br/>[Componente: Spring Data JPA]<br/>OrdemServicoRepositoryImpl, ServicoRepositoryImpl"]
            gw_veiculos["<b>Gateway de cadastro</b><br/>[Componente: porta CadastroDeVeiculos]<br/>CadastroDeVeiculosImpl"]
            gw_estoque["<b>Gateway de estoque</b><br/>[Componente: porta EstoqueDeItens]<br/>EstoqueDeItensImpl"]
        end

        subgraph ctx_estoque["Peças e Insumos"]
            direction TB
            ctrl_estoque["<b>Controller de estoque</b><br/>[Componente: REST Controller]<br/>ItemEstoqueController"]
            uc_estoque["<b>Casos de uso de estoque</b><br/>[Componente: Java]<br/>CadastrarItem, RegistrarEntrada, BaixarItens, ..."]
            repo_estoque["<b>Repositório de estoque</b><br/>[Componente: Spring Data JPA]<br/>ItemEstoqueRepositoryImpl"]
        end
    end

    bd[("<b>Banco de dados</b><br/>[Container: PostgreSQL 16]")]

    funcionario -->|"Login [JSON/HTTPS]"| auth
    funcionario -->|"Chamadas com JWT"| seguranca
    cliente -->|"Número da OS + CPF/CNPJ, sem login"| ctrl_portal

    seguranca --> ctrl_cliente
    seguranca --> ctrl_os
    seguranca --> ctrl_estoque

    ctrl_cliente --> uc_cliente
    ctrl_os --> uc_os
    ctrl_portal -->|"Consulta, aprova e rejeita"| uc_os
    ctrl_estoque --> uc_estoque

    uc_os -->|"Identifica cliente e veículo"| gw_veiculos
    uc_os -->|"Cota itens e dá baixa na aprovação"| gw_estoque
    gw_veiculos -->|"ConsultarClientes, ConsultarVeiculos"| uc_cliente
    gw_estoque -->|"ConsultarItens, BaixarItens"| uc_estoque

    uc_cliente --> repo_cliente
    uc_os --> repo_os
    uc_estoque --> repo_estoque

    repo_cliente -->|"JDBC/SQL"| bd
    repo_os -->|"JDBC/SQL"| bd
    repo_estoque -->|"JDBC/SQL"| bd

    classDef pessoa fill:#08427b,stroke:#052e56,color:#fff
    classDef container fill:#438dd5,stroke:#2e6295,color:#fff
    classDef componente fill:#85bbf0,stroke:#5d82a8,color:#000
    class funcionario,cliente pessoa
    class bd container
    class auth,seguranca,ctrl_cliente,uc_cliente,repo_cliente,ctrl_os,ctrl_portal,uc_os,repo_os,gw_veiculos,gw_estoque,ctrl_estoque,uc_estoque,repo_estoque componente
    style api fill:#ffffff,stroke:#444,stroke-dasharray:6 4,color:#000
    style ctx_cliente fill:#f4f8fc,stroke:#5d82a8,color:#000
    style ctx_os fill:#f4f8fc,stroke:#5d82a8,color:#000
    style ctx_estoque fill:#f4f8fc,stroke:#5d82a8,color:#000
```

## Como ler

- **Camadas:** controllers (`infrastructure/http`) chamam casos de uso (`application`), que aplicam as regras do domínio (`domain`) e persistem pelos repositórios (`infrastructure/db`). `application` e `domain` não dependem de Spring.
- **Transação:** abre no controller, então aprovar o orçamento e dar baixa nos itens acontecem juntos ou não acontecem.
- **Entre contextos:** a OS não conhece as classes de cliente nem de estoque. Ela declara as portas `CadastroDeVeiculos` e `EstoqueDeItens` no domínio, e os gateways em `infrastructure/gateway` as implementam chamando os casos de uso do outro contexto.
- **Erros:** o `ErroHandler` (omitido no diagrama) converte as exceções de todas as camadas no formato padrão de erro da API.
- **Cobrança e Faturamento:** contexto reservado, sem implementação no MVP.
