# Modelo C4 — Contexto

O sistema de gestão da oficina é um único sistema, usado pelo funcionário com login (JWT) e pelo cliente sem login, que se identifica com o número da OS e o CPF/CNPJ do titular. No MVP ele se integra só com o servidor de e-mail, que recebe a notificação de cada mudança de status da OS.

```mermaid
---
title: Contexto - Sistema de gestão da oficina mecânica
config:
  flowchart:
    curve: basis
    wrappingWidth: 260
    nodeSpacing: 60
    rankSpacing: 120
---
flowchart TB
    funcionario(["<b>Funcionário</b><br/>[Pessoa]<br/>Atende o cliente, conduz a OS e controla o estoque"])
    cliente(["<b>Cliente</b><br/>[Pessoa]<br/>Dono do veículo"])
    email["<b>Servidor de e-mail</b><br/>[Sistema externo]<br/>Entrega ao cliente o e-mail de mudança de status (Mailpit na execução local)"]
    sistema["<b>Sistema de gestão da oficina mecânica</b><br/>[Sistema]<br/>Clientes e veículos, catálogo de serviços, peças e insumos, ordens de serviço e orçamentos"]

    funcionario -->|"Cadastra clientes, veículos, serviços e itens; abre e conduz OS; registra entradas de estoque; consulta listagem e tempo médio<br/>[JSON/HTTPS, JWT]"| sistema
    cliente -->|"Consulta o andamento e aprova ou rejeita o orçamento informando número da OS e CPF/CNPJ<br/>[JSON/HTTPS, sem login]"| sistema
    sistema -->|"Envia o e-mail de status da OS depois do commit<br/>[SMTP]"| email
    email -.->|"Entrega o e-mail"| cliente

    classDef pessoa fill:#08427b,stroke:#052e56,color:#fff
    classDef sistema fill:#1168bd,stroke:#0b4884,color:#fff
    classDef externo fill:#999,stroke:#6b6b6b,color:#fff
    class funcionario,cliente pessoa
    class sistema sistema
    class email externo
```

## Fora do MVP

O Event Storming e o board do Figma também mapeiam sistemas externos que não foram implementados: sistema de pagamentos, emissão de nota fiscal e fornecedor de itens. O motivo de cada corte está em [`.specs/ROADMAP.md`](../../../.specs/ROADMAP.md) (AD-003).
