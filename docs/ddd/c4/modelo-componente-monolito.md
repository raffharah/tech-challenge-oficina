---
title: Modelo de Componente Monolito
config:
  flowchart:
    curve: basis
    wrappingWidth: 220
    nodeSpacing: 120
    rankSpacing: 160
---
flowchart LR
    cliente(["<b>Cliente</b><br/>[Pessoa]"])
    funcionario(["<b>Funcionário</b><br/>[Pessoa]"])

    api["<b>Serviço de API</b><br/>[Container]"]
    subgraph monolito["Monolito [Container]"]
        ctrl_auth["<b>Controller de autenticação</b><br/>[Componente: MVC Rest Controller]"]
        ctrl_os["<b>Controller de ordens de serviço</b><br/>[Componente: MVC Rest Controller]"]
        ctrl_estoque["<b>Controller de estoque</b><br/>[Componente: MVC Rest Controller]"]
        ctrl_dados["<b>Controller de dados cadastrais</b><br/>[Componente: MVC Rest Controller]"]
        f_pagamento["<b>Fachada de Pagamento</b><br/>[Componente: Java Bean]"]
        f_nf["<b>Fachada de Emissão de nota fiscal</b><br/>[Componente: Java Bean]"]
        notificacoes["<b>Componente de Notificações</b><br/>[Componente: Java Bean]"]
        persistencia["<b>Componente de persistência</b><br/>[Componente: Java Bean]"]
        seguranca["<b>Componente de segurança</b><br/>[Componente: Java Bean]"]
        f_fornecedor["<b>Fachada de fornecedor de itens</b><br/>[Componente: Java Bean]"]
    end

    bd[("<b>Banco de dados</b><br/>[Container: PostgreSQL]")]
    pagamentos["<b>Sistema de pagamentos</b><br/>[Sistema externo]"]
    nf["<b>Sistema de emissão de nota fiscal</b><br/>[Sistema externo]"]
    fornecedor["<b>Sistema de fornecedor de itens</b><br/>[Sistema externo]"]
    email["<b>Provedor de caixa de e-mails</b><br/>[Sistema externo]"]

    ctrl_dados --> seguranca
    ctrl_os -->|"Obtém informações sobre estado de pagamentos"| f_pagamento
    ctrl_auth --> persistencia
    ctrl_auth -->|"Valida credenciais de acesso"| seguranca
    ctrl_os -->|"Envia detalhes dos serviços prestados para"| f_nf
    f_pagamento -->|"Solicita o pagamento usando [JSON/HTTPS]"| pagamentos

    funcionario --> ctrl_estoque
    ctrl_estoque --> persistencia
    funcionario --> ctrl_dados
    funcionario -->|"Manda chamada de API para"| ctrl_auth
    cliente --> ctrl_os
    ctrl_estoque -->|"Recupera informações sobre orçamentos"| f_fornecedor
    cliente -->|"Manda chamada de API para"| ctrl_auth
    f_fornecedor -->|"Solicita reposição e garantia de itens usando [JSON/HTTPS]"| fornecedor
    api -->|"Valida tokens de autenticação"| seguranca
    ctrl_dados --> persistencia
    ctrl_os -->|"Solicita envio de notificações"| notificacoes
    ctrl_os -->|"Persiste e recupera dados usando"| persistencia

    funcionario --> ctrl_os
    notificacoes -->|"Envio de e-mails usando"| email
    f_nf -->|"Solicita a emissão da nota fiscal usando [JSON/HTTPS]"| nf
    ctrl_estoque -->|"Valida tokens de autenticação"| seguranca
    persistencia -->|"Lê e grava em [SQL/TCP]"| bd

    classDef pessoa fill:#08427b,stroke:#052e56,color:#fff
    classDef container fill:#438dd5,stroke:#2e6295,color:#fff
    classDef componente fill:#85bbf0,stroke:#5d82a8,color:#000
    classDef externo fill:#999999,stroke:#6b6b6b,color:#fff
    class cliente,funcionario pessoa
    class api,bd container
    class ctrl_auth,ctrl_os,ctrl_estoque,ctrl_dados,seguranca,persistencia,notificacoes,f_pagamento,f_nf,f_fornecedor componente
    class pagamentos,nf,fornecedor,email externo
    style monolito fill:#ffffff,stroke:#444,stroke-dasharray:6 4,color:#000