## Validation: ajustes-revisao - PASS ✅

**Diff range**: a3862f8..HEAD (`fix/ajustes-revisao-fase1`)
**Gate**: `./mvnw clean verify` → 247 testes unitários e 171 de integração, 0 falhas, 0 skips, BUILD SUCCESS. JaCoCo: 100% de linhas em todos os pacotes de `domain` e `application`.
**Aceitação de ponta a ponta**: o roteiro do vídeo (`docs/roteiro-video.md`, cenas 1 a 6) foi executado contra a aplicação do `docker compose`, com banco limpo e seed: 52 passos, 0 falhas.

### Cobertura por AC

Prefixos: `D` = `src/test/java/br/com/fiap/oficina/domain`, `A` = `src/test/java/br/com/fiap/oficina/application`, `H` = `src/test/java/br/com/fiap/oficina/infrastructure/http`, `B` = `src/test/java/br/com/fiap/oficina/infrastructure/db`, `C` = `src/test/java/br/com/fiap/oficina/infrastructure/config`.

| AC | Evidência (`file:line`) | Resultado |
| --- | --- | --- |
| REV-01.1 | `C/OpenApiIT.java:38` toda operação com `summary`, `description` e tags; toda tag com descrição | ✅ |
| REV-01.2 | `C/OpenApiIT.java:53` campos de `*Request`; `:70` parâmetros com descrição e exemplo | ✅ |
| REV-01.3 | `C/OpenApiIT.java:92` 422 do orçamento cita `ORDEM_SEM_SERVICO` e usa `ErroResponse`; 401 só nas rotas com JWT | ✅ |
| REV-02.1 | `H/cliente/ClienteControllerIT.java:131` sem filtro; `:116` paginação; `A/cliente/ConsultarClientesTest.java:62`, `:82` | ✅ |
| REV-02.2 | `H/cliente/ClienteControllerIT.java:143` máscara e CNPJ alfanumérico; `A/cliente/ConsultarClientesTest.java:32`, `:42` | ✅ |
| REV-02.3 | `H/cliente/ClienteControllerIT.java:158` página vazia e 422; `A/cliente/ConsultarClientesTest.java:49`, `:57` | ✅ |
| REV-02/03 Swagger | `C/OpenApiIT.java:81` `GET /clientes` e `GET /veiculos` sem parâmetro obrigatório | ✅ |
| REV-03.1 | `H/cliente/VeiculoControllerIT.java:81`; `B/cliente/VeiculoRepositoryImplIT.java:73`; `A/cliente/ConsultarVeiculosTest.java:44` | ✅ |
| REV-03.2 | `H/cliente/VeiculoControllerIT.java:64`; `A/cliente/ConsultarVeiculosTest.java:35` | ✅ |
| REV-03.3 | `H/cliente/VeiculoControllerIT.java:98` (422), `:195` (página vazia); `A/cliente/ConsultarVeiculosTest.java:57` | ✅ |
| REV-04.1 | `H/ordemservico/ConsultaClienteIT.java:112` sem token, só do titular, ordem e campos; `A/ordemservico/ListarOrdensDoClienteTest.java:23`, `:37`; `B/ordemservico/OrdemServicoRepositoryImplIT.java:150` | ✅ |
| REV-04.2 | `H/ordemservico/ConsultaClienteIT.java:133`; `A/ordemservico/ListarOrdensDoClienteTest.java:49` | ✅ |
| REV-04.3 | `H/ordemservico/ConsultaClienteIT.java:104` (422), `:133` (400) | ✅ |
| REV-05 | `docs/ddd/modelo-de-dominio.md`; Decisões do MVP em `docs/ddd/linguagem-ubiqua.md` | ✅ (documental) |
| REV-06.1 | `D/ordemservico/OrdemServicoTest.java:37`; `A/ordemservico/AbrirOrdemServicoTest.java:25`; `H/ordemservico/OrdemServicoAberturaIT.java:45` | ✅ |
| REV-06.2 | `D/ordemservico/OrdemServicoTest.java:49` (data própria); `A/ordemservico/IniciarDiagnosticoTest.java:24`; `H/ordemservico/OrdemServicoAberturaIT.java:65` | ✅ |
| REV-06.3 | `D/ordemservico/OrdemServicoTest.java:59`; `A/ordemservico/IniciarDiagnosticoTest.java:37`; `H/ordemservico/OrdemServicoAberturaIT.java:76`; 404 em `:97`; 401 em `:102` | ✅ |
| REV-06.4 | `D/ordemservico/OrdemServicoTest.java:68`; `H/ordemservico/OrdemServicoAberturaIT.java:86` | ✅ |

### Pendências fora do código

- Board do Figma e `docs/ddd/event-storming/fluxo-os.png`: trocar a política "Sistema atualiza a OS para em diagnóstico" (passos 6 e 7) pelo comando "Inicia diagnóstico" do Funcionário, como já está em `fluxo-os.md`.
