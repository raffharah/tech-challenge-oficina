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
        auth["<b>AutenticacaoController</b><br/>[Componente: REST Controller]<br/>Recebe as credenciais, valida usuário e senha e emite o token JWT"]
        seguranca["<b>SecurityConfig</b><br/>[Componente: Spring Security]<br/>Autentica tokens JWT, aplica autorização e protege as rotas administrativas"]

        subgraph ctx_cliente["Clientes e Veículos"]
            direction TB
            ctrl_cliente["<b>ClienteController e VeiculoController</b><br/>[Componente: REST Controller]<br/>Cadastra, atualiza, consulta, lista e remove clientes e veículos"]
            uc_cliente["<b>Casos de uso de Clientes e Veículos</b><br/>[Componente: Application Services]<br/>Executa os comandos e consultas de clientes e veículos"]
            repo_cliente["<b>ClienteRepositoryImpl e VeiculoRepositoryImpl</b><br/>[Componente: Spring Data JPA]<br/>Persiste clientes e veículos no PostgreSQL"]
        end

        subgraph ctx_os["Ordens de Serviço"]
            direction TB
            ctrl_os["<b>OrdemServicoController</b><br/>[Componente: REST Controller]<br/>Abre a OS, inicia o diagnóstico, gera orçamento, finaliza e entrega veículos"]
            ctrl_servicos["<b>ServicoController</b><br/>[Componente: REST Controller]<br/>Gerencia o catálogo de serviços, preços padrão, consulta, atualização e remoção"]
            ctrl_portal["<b>ClienteOrdemServicoController</b><br/>[Componente: REST Controller]<br/>Lista as OS do titular, consulta andamento, aprova ou rejeita a OS com o CPF/CNPJ do titular"]
            uc_os["<b>Casos de uso de Ordens de Serviço</b><br/>[Componente: Application Services]<br/>Orquestra abertura, componentes, orçamento, execução, finalização e entrega da OS"]
            repo_os["<b>OrdemServicoRepositoryImpl e ServicoRepositoryImpl</b><br/>[Componente: Spring Data JPA]<br/>Persiste ordens, histórico de status, serviços e componentes da OS"]
            gw_veiculos["<b>CadastroDeVeiculosImpl</b><br/>[Componente: Gateway]<br/>Implementa CadastroDeVeiculos e resolve o veículo associado à OS em outro contexto"]
            gw_estoque["<b>EstoqueDeItensImpl</b><br/>[Componente: Gateway]<br/>Implementa EstoqueDeItens para cotar itens, verificar disponibilidade e registrar baixas"]
        end

        subgraph ctx_estoque["Peças e Insumos"]
            direction TB
            ctrl_estoque["<b>ItemEstoqueController</b><br/>[Componente: REST Controller]<br/>Gerencia itens, consulta saldos, atualiza dados e registra entradas físicas"]
            uc_estoque["<b>Casos de uso de Peças e Insumos</b><br/>[Componente: Application Services]<br/>Executa cadastro, consulta, atualização, entrada, baixa e remoção de itens"]
            repo_estoque["<b>ItemEstoqueRepositoryImpl</b><br/>[Componente: Spring Data JPA]<br/>Persiste itens, saldos físicos e controle de versão do estoque"]
        end
    end

    bd[("<b>Banco de dados</b><br/>[Container: PostgreSQL 16]")]

    funcionario -->|"Envia usuário e senha [JSON/HTTPS]"| auth
    auth -->|"Retorna token JWT"| funcionario
    funcionario -->|"Envia requisições com Bearer token [JSON/HTTPS]"| seguranca
    cliente -->|"Informa CPF/CNPJ e número da OS [JSON/HTTPS]"| ctrl_portal

    seguranca -->|"Autoriza operações administrativas"| ctrl_cliente
    seguranca -->|"Autoriza operações sobre ordens"| ctrl_os
    seguranca -->|"Autoriza operações sobre serviços"| ctrl_servicos
    seguranca -->|"Autoriza operações sobre estoque"| ctrl_estoque

    ctrl_cliente -->|"Cadastra, consulta, atualiza e remove clientes e veículos"| uc_cliente
    ctrl_os -->|"Abre, compõe, orça, acompanha, finaliza e entrega ordens"| uc_os
    ctrl_servicos -->|"Cadastra, consulta, atualiza, lista e remove serviços"| uc_os
    ctrl_portal -->|"Lista e consulta as OS, aprova ou rejeita o orçamento"| uc_os
    ctrl_estoque -->|"Cadastra itens, atualiza dados, consulta saldo e registra entradas"| uc_estoque

    uc_os -->|"Resolve cliente e veículo identificado pela OS"| gw_veiculos
    uc_os -->|"Cota itens, verifica saldo disponível e dá baixa na aprovação"| gw_estoque
    gw_veiculos -->|"Consulta clientes e veículos nos casos de uso do contexto"| uc_cliente
    gw_estoque -->|"Consulta itens, disponibilidade e executa baixas"| uc_estoque

    uc_cliente -->|"Consulta ou persiste clientes e veículos"| repo_cliente
    uc_os -->|"Consulta ou persiste ordens, serviços e histórico de status"| repo_os
    uc_estoque -->|"Consulta ou persiste itens e saldos"| repo_estoque

    repo_cliente -->|"Lê e grava clientes e veículos [JDBC/SQL]"| bd
    repo_os -->|"Lê e grava ordens e catálogo de serviços [JDBC/SQL]"| bd
    repo_estoque -->|"Lê e grava itens e saldos [JDBC/SQL]"| bd

    classDef pessoa fill:#08427b,stroke:#052e56,color:#fff
    classDef container fill:#438dd5,stroke:#2e6295,color:#fff
    classDef componente fill:#85bbf0,stroke:#5d82a8,color:#000
    class funcionario,cliente pessoa
    class bd container
    class auth,seguranca,ctrl_cliente,ctrl_os,ctrl_servicos,ctrl_portal,uc_os,repo_os,gw_veiculos,gw_estoque,ctrl_estoque,uc_estoque,repo_estoque,uc_cliente,repo_cliente componente
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
