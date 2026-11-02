# Ajustes da revisão Tasks

**Spec**: `.specs/features/ajustes-revisao/spec.md`
**Status**: Implemented
**Branch**: `fix/ajustes-revisao-fase1`

| # | Task | Requisito | Commit | Status |
| --- | --- | --- | --- | --- |
| T1 | `OrdemServico.abrir` deixa a OS `RECEBIDA`; `iniciarDiagnostico`, caso de uso `IniciarDiagnostico` e `POST /ordens-servico/{id}/diagnostico`; fixtures e ITs passam pelo diagnóstico; Event Storming, linguagem ubíqua, README e roteiro atualizados | REV-06 | `a3e8cd5` | Done |
| T2 | `Pagina.de`; `ConsultarClientes.listar(cpfCnpj, pagina, tamanho)`; `GET /clientes` único com filtro opcional | REV-02 | `a48e4d2` | Done |
| T3 | `VeiculoRepository.listar`; `ConsultarVeiculos.listar(placa, pagina, tamanho)`; `GET /veiculos` paginado com filtro opcional | REV-03 | `5ff5e4f` | Done |
| T4 | `OrdemServicoRepository.listarPorCpfCnpj`; caso de uso `ListarOrdensDoCliente`; `GET /cliente/ordens-servico`; AD-006, AD-007 e revisão da AD-001 | REV-04 | `2a0c5ee` | Done |
| T5 | `@Tag`, `@Operation`, `@ApiResponse` e `@Parameter` nos controllers; `@Schema` nos requests; `OpenApiConfig` com descrição geral, `ErroResponse` em 4xx e 401 nas rotas com JWT; `OpenApiIT` exige a cobertura | REV-01 | `9f8f7fd` | Done |
| T6 | `docs/ddd/modelo-de-dominio.md`; Decisões do MVP alinhadas ao código | REV-05 | `0acbcc7` | Done |
| T7 | Roteiro do vídeo com listagens e documentação; C4, ROADMAP e specs antigas com as revisões; esta spec | todos | último commit da branch | Done |
| T8 | Serviço ou item inexistente na OS responde 404; AD-008; `AGENTS.md` com exemplos reais | REV-07 | ver `git log` | Done |
| T9 | `OrdemServico.getAbertaEm`; `abertaEm` em `OrdemServicoResumoDTO` | REV-08 | ver `git log` | Done |

## Gate

`./mvnw verify` (unitários, ArchUnit, ITs com Testcontainers e cobertura JaCoCo ≥ 80% em `domain` e `application`).
