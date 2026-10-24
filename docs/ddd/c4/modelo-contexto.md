---
title: Modelo de Contexto - Sistema de gestão de ordens de serviço
config:
  flowchart:
    curve: basis
    wrappingWidth: 260
    nodeSpacing: 60
    rankSpacing: 200
---
flowchart LR
    cliente(["<b>Cliente</b><br/>[Pessoa]"])
    funcionario(["<b>Funcionário</b><br/>[Pessoa]"])
    sgos["<b>Sistema de gestão de<br/>ordens de serviço</b><br/>[Sistema]"]
    estoque["<b>Sistema de estoque<br/>de itens</b><br/>[Sistema]"]
    email["<b>Provedor de caixa<br/>de e-mails</b><br/>[Sistema externo]"]
    pagamentos["<b>Sistema de pagamentos</b><br/>[Sistema externo]"]
    nf["<b>Sistema de emissão<br/>de nota fiscal</b><br/>[Sistema externo]"]
    fornecedor["<b>Sistema de fornecedor<br/>de itens</b><br/>[Sistema externo]"]

    funcionario -->|"Identifica o cliente, abre e acompanha ordens de serviço usando"| sgos
    email -->|"Envia e-mails para"| cliente
    cliente -->|"Aprova ou recusa orçamentos e consulta o andamento da OS usando"| sgos
    funcionario -->|"Cadastra, atualiza, inativa itens, registra entradas usando"| estoque
    sgos -->|"Envio de e-mails usando"| email
    sgos -->|"Consulta disponibilidade, preço, reserva e consome itens usando"| estoque
    sgos -->|"Solicita o pagamento usando"| pagamentos
    sgos -->|"Solicita a emissão da nota fiscal usando"| nf
    estoque -->|"Solicita reposição e garantia de itens usando"| fornecedor

    classDef pessoa fill:#08427b,stroke:#052e56,color:#fff
    classDef sistema fill:#1168bd,stroke:#0b4884,color:#fff
    classDef externo fill:#999999,stroke:#6b6b6b,color:#fff
    class cliente,funcionario pessoa
    class sgos,estoque sistema
    class email,pagamentos,nf,fornecedor externo
