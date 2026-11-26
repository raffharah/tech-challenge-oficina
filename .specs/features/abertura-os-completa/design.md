# Abertura de OS Completa Design

**Spec**: `.specs/features/abertura-os-completa/spec.md`
**Decisões**: AD-011 (revisa AD-007), AD-008, AD-004

## Fluxo

```
POST /ordens-servico  (@Transactional no controller)
  AbrirOrdemServicoRequest ── @Valid: cpfCnpj, email, placa @NotBlank; servicos, itens @NotEmpty
        │
        ▼
  AbrirOrdemServico.executar(AbrirOrdemServicoDTO)
    1. veiculos.identificar(cpfCnpj, placa)            → VeiculoIdentificado (404/422 como hoje)
    2. servicos.buscarPorId(id) para cada serviço      → Servico (404 SERVICO_NAO_ENCONTRADO)
    3. estoque.cotar(itemId) para cada item            → ItemCotado (404 ITEM_NAO_ENCONTRADO)
    4. OrdemServico.abrir(veiculo, servicos, itens, agora)  → RECEBIDA com componentes
    5. veiculos.registrarEmail(cpfCnpj, email)         → grava no Cliente (422 EMAIL_INVALIDO)
    6. ordens.salvar(ordem)
```

Qualquer exceção desfaz a transação inteira, inclusive o e-mail gravado no passo 5 (ABR-04).

## Domínio

### `domain/cliente/Email` (novo objeto de valor)

- Construtor valida o formato `^[^@\s]+@[^@\s]+\.[^@\s]+$` depois de `trim()` e guarda o valor em minúsculas.
- Formato inválido ou vazio lança `DomainException("EMAIL_INVALIDO", ...)`, que vira 422.
- Segue o padrão de `CpfCnpj` e `Placa`.

### `domain/cliente/Cliente` (alterado)

- Ganha o campo `Email email`, que pode ser nulo, e o método `registrarEmail(Email)`.
- O construtor passa a receber `email`. `CadastrarCliente` e `AtualizarCliente` mantêm o e-mail existente: o CRUD não muda o e-mail (fora do escopo).

### `domain/ordemservico/ItemSolicitado` (novo record)

- `ItemSolicitado(ItemCotado item, int quantidade)`: item pedido na abertura, já cotado.

### `domain/ordemservico/OrdemServico` (alterado)

- `abrir(VeiculoIdentificado, List<Servico>, List<ItemSolicitado>, Instant)` substitui `abrir(VeiculoIdentificado, Instant)`.
- A regra de serviço repetido (`SERVICO_JA_INCLUIDO`), quantidade (`QUANTIDADE_INVALIDA`) e soma de item repetido passa para métodos privados `adicionarServico` e `adicionarItem`.
- `abrir` chama esses métodos sem a guarda de diagnóstico. `incluirServico` e `incluirItem` chamam `exigirEmDiagnostico()` e depois os mesmos métodos privados. A regra não se duplica.
- `total` continua nulo até `concluirDiagnostico`.

### `domain/ordemservico/CadastroDeVeiculos` (porta alterada)

- Ganha `void registrarEmail(String cpfCnpj, String email)`.

## Application

- `AbrirOrdemServicoDTO(String cpfCnpj, String email, String placa, List<Long> servicos, List<ItemSolicitadoDTO> itens)`, com `ItemSolicitadoDTO(Long itemId, int quantidade)`.
- O construtor de `AbrirOrdemServico` passa a receber também `ServicoRepository` e `EstoqueDeItens`, já usados por `IncluirServicoNaOrdem` e `IncluirItemNaOrdem`.
- `application/cliente/RegistrarEmailDoCliente` (novo): `executar(String cpfCnpj, String email)`. Busca o cliente pelo CPF/CNPJ (404 `CLIENTE_NAO_ENCONTRADO`), chama `registrarEmail(new Email(email))` e salva.
- `ClienteDTO` ganha `email`, que pode ser nulo. A notificação lê o e-mail por ele.

## Infraestrutura

- `V5__cliente_email.sql`: `alter table cliente add column email varchar(254);`
- `ClienteEntity.email` e o mapeamento em `ClienteRepositoryImpl`.
- `CadastroDeVeiculosImpl.registrarEmail` chama `RegistrarEmailDoCliente`, seguindo o padrão de `identificar`, que chama `ConsultarClientes`.
- `AbrirOrdemServicoRequest`:
  - `cpfCnpj`, `email`, `placa`: `@NotBlank`;
  - `servicos`: `@NotEmpty List<@NotNull Long>`;
  - `itens`: `@NotEmpty List<@Valid ItemSolicitadoRequest>`, com `ItemSolicitadoRequest(@NotNull Long itemId, @NotNull Integer quantidade)` como record aninhado;
  - `@Schema` com exemplo em cada campo.
- `OrdemServicoController.abrir`: conversão para o DTO e `@Operation` atualizada.
- `BeanConfig`: novos parâmetros de `AbrirOrdemServico` e o bean `RegistrarEmailDoCliente`.

## Impacto em testes existentes

- `OrdemServico.abrir(veiculo, agora)` deixa de existir. Precisam usar a assinatura nova:
  - fixtures `OrdemServicoEmDiagnostico`, `OrdemServicoAguardandoAprovacao`, `OrdemServicoEmExecucao`;
  - `OrdemServicoTest`, `ListarOrdensServicoTest`, `ListarOrdensDoClienteTest`, `IniciarDiagnosticoTest`, `OrdemServicoRepositoryImplIT`.
- As ITs que abrem OS pela API passam a enviar o corpo novo: `OrdemServicoAberturaIT`, `OrdemServicoComponentesIT`, `OrdemServicoOrcamentoIT`, `DecisaoOrcamentoIT`, `ExecucaoEntregaIT`, `ConsultaClienteIT`, `AcompanhamentoAdminIT`.
- `new Cliente(...)` ganha o parâmetro `email` em `ClienteTest`, `ClienteRepositoryEmMemoria` e nos testes de `application/cliente`.

## Documentação

- `docs/ddd/modelo-de-dominio.md`: objeto de valor `Email` no agregado Cliente e componentes da OS na abertura.
- `docs/ddd/linguagem-ubiqua.md`, Decisões do MVP: "Início do diagnóstico" e "Agregados".
