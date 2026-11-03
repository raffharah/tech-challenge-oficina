# Roteiro complementar 1 — apresentação, README, arquitetura e execução

Vídeo sugerido: cerca de 4 minutos. Este roteiro complementa a demonstração funcional da API.

## Cena 1 — Identificação do grupo e proposta (0:45)

> Fala: "Somos o Grupo 96 do Tech Challenge da Fase 1, SOAT — Software Architecture. Este projeto é o back-end do sistema integrado de uma oficina mecânica."

Apresente a tela do README ou do PDF e identifique visualmente todos os integrantes, RMs e usernames do Discord:

| Nome | RM | Username no Discord |
| --- | --- | --- |
| Emigdio Bertolo Rizardi | rm377323 | emigdioriz |
| Vitória da Silva Berber | rm378173 | Vitória Berber - RM378173 |
| Matheus Luís Webber | rm378161 | Matheus Webber - rm378161 |
| Rafael Massakazo Hara | rm378138 | Hara |
| Raiana Rocha Oliveira | rm378287 | Rana |


> Fala: "A oficina tinha processos manuais e planilhas, com dificuldades para controlar o histórico, o andamento dos serviços, os orçamentos, as peças e os insumos. Nosso MVP organiza esse fluxo por API REST."

Mostre os objetivos presentes na seção **Objetivo** do README:

- organizar o atendimento e o ciclo de vida das Ordens de Serviço;
- controlar clientes, veículos, serviços, peças e insumos;
- acompanhar o andamento, gerar e aprovar orçamentos;
- oferecer autenticação e validação para a operação administrativa.

## Cena 2 — README e escopo do MVP (0:30)

Abra o README do repositório e mostre:

- objetivo e descrição da solução;
- os três contextos implementados: clientes e veículos, ordens de serviço e estoque;
- o contexto de cobrança e faturamento como reservado e fora do MVP;
- pré-requisitos, tecnologias e instruções de execução;
- documentação DDD, OpenAPI, testes e evidências de entrega.

> Fala: "O contexto de cobrança está modelado para evolução futura, mas não faz parte do MVP desta fase."

## Cena 3 — DDD e Event Storming (0:30)

Abra os documentos exportados ou os quadros vinculados no README.

> Fala: "A linguagem ubíqua orienta nomes e regras, como Ordem de Serviço, orçamento, peças, insumos, abertura, aprovação e status."

Apresente:

- comandos, eventos, políticas e atores;
- agregados e entidades;
- objetos de valor como `CpfCnpj` e `Placa`;
- contextos de clientes, ordens de serviço e estoque;
- relação entre documentação e implementação.

Mostre a relação entre um status da documentação e a implementação em `OrdemServico`.

## Cena 4 — Arquitetura e organização das camadas (0:45)

Abra o diagrama C4 de componentes e depois a estrutura de diretórios do projeto.

> Fala: "A solução é um monólito modular com Clean Architecture e DDD. Ela tem um único deployable, mas os contextos ficam isolados por pacotes."

Explique a direção das dependências:

```text
infrastructure → application → domain
```

| Camada | Responsabilidade |
| --- | --- |
| `domain` | entidades, objetos de valor, regras, exceções, repositórios e portas |
| `application` | casos de uso, orquestração e DTOs |
| `infrastructure` | Spring, HTTP, persistência JPA, gateways, segurança e configuração |

Mostre na IDE os seguintes exemplos, conforme os componentes do modelo C4:

- **`domain`:** `OrdemServico` representa o agregado de Ordens de Serviço, enquanto `CpfCnpj` e `Placa` são objetos de valor. A porta `CadastroDeVeiculos` corresponde ao gateway que resolve veículos e a porta `EstoqueDeItens` corresponde ao gateway de peças e insumos.
- **`application`:** `AbrirOrdemServico` corresponde ao caso de uso de abertura da OS que o modelo chama de `Casos de uso de Ordens de Serviço`. Também podem ser mostrados `AprovarOrcamento` ou `BaixarItens`.
- **`infrastructure` — controller:** `OrdemServicoController`, correspondente ao componente `OrdemServicoController` do modelo; ele recebe a abertura, o diagnóstico, o orçamento, a finalização e a entrega da OS.
- **`infrastructure` — repository implementation:** `OrdemServicoRepositoryImpl`, correspondente ao componente `OrdemServicoRepositoryImpl e ServicoRepositoryImpl`; ele persiste ordens, serviços e histórico de status no PostgreSQL.

> Fala: "A regra de negócio fica no domínio, enquanto Spring e banco ficam em infraestrutura. Os contextos se comunicam por portas."

## Cena 5 — Subir o ambiente com Docker Compose (0:30)

Na raiz do projeto, mostre o `docker-compose.yml` e execute:

```powershell
docker compose down -v
docker compose up --build
```

Explique os serviços:

| Serviço | Função |
| --- | --- |
| `postgres` | banco PostgreSQL 16 persistente |
| `migration` | aplica migrations Flyway |
| `seed` | carga o catálogo inicial |
| `app` | compila/executa a API na porta 8080 |

> Fala: "O `down -v` é usado antes da gravação para reiniciar os IDs e os saldos do cenário. Em seguida, o Compose cria o banco, aplica o schema, carrega o catálogo e inicia a aplicação."

## Cena 6 — Confirmar que a aplicação está pronta (0:20)

Aguarde a API iniciar e confirme a disponibilidade.

Abra:

- Swagger: <http://localhost:8080/swagger-ui/index.html>
- OpenAPI: <http://localhost:8080/v3/api-docs>

> Fala: "A aplicação está pronta. A próxima etapa mostra o fluxo real pela API, usando JWT nas rotas administrativas e rotas públicas para consulta e decisão do cliente."
