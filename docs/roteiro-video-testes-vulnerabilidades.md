# Roteiro complementar 2 — testes automatizados e vulnerabilidades

Vídeo sugerido: cerca de 3 minutos. A gravação deve ser feita com Docker disponível e com os artefatos de evidência abertos.

## Cena 1 — Estrutura dos testes (0:30)

Abra o diretório `src/test/java/br/com/fiap/oficina` e explique que ele espelha os contextos da aplicação.

> Fala: "Os testes estão separados pelo tipo de comportamento. Os testes unitários e de aplicação usam objetos em memória; as integrações usam PostgreSQL real por meio de Testcontainers."

| Tipo | Identificação | Finalidade |
| --- | --- | --- |
| Unitário | `*Test` em `domain`/`application` | regras isoladas, casos de uso e dependências em memória |
| Arquitetura | `ArquiteturaTest` | direção das camadas e isolamento entre contextos |
| Integração | `*IT` em `infrastructure` | API, segurança, banco, JPA e integrações HTTP |

## Cena 2 — Executar a verificação completa (0:45)

Execute:

```powershell
.\mvnw.cmd verify
```

Explique que `verify` executa:

- testes unitários de domínio e aplicação;
- testes de arquitetura;
- testes de integração com Testcontainers;
- geração do relatório JaCoCo;
- verificação de cobertura mínima de 80% nos pacotes críticos de domínio e aplicação.

Enquanto o Maven executa, explique:

- testes de domínio verificam CPF/CNPJ, placa, saldo, status e transições;
- testes de aplicação exercitam os casos de uso;
- ArchUnit impede inversões indevidas de dependência;
- os testes unitários executam sem Spring ou banco;
- os testes de integração validam API, segurança, banco, JPA e JWT com PostgreSQL real.

> Fala: "A integração sobe PostgreSQL em ambiente descartável. Assim, testes de JPA, migrations, repositories, controllers e JWT são feitos com comportamento próximo ao de produção."

Após concluir, abra:

- `target/site/jacoco/index.html`;
- diretórios de relatório em `target/surefire-reports`;
- relatórios de integração em `target/failsafe-reports`.

## Cena 3 — Interpretar cobertura e evidências (0:25)

Na página do JaCoCo, localize os pacotes críticos e explique o conceito:

- cobertura de linha indica quantas linhas executáveis foram percorridas pelos testes;
- o projeto exige pelo menos 80% nos pacotes de `domain` e `application`;

> Fala: "Não mostramos apenas o total geral: o projeto restringe a regra `COVEREDRATIO` aos pacotes críticos definidos no `pom.xml`."

Mostre os resultados positivos dos testes de fluxo, por exemplo:

- validação e cadastro;
- transições da OS;
- aprovação e rejeição;
- controle de estoque;
- autenticação e OpenAPI.

Se alguma evidência estiver desatualizada, execute novamente antes da gravação e use somente os resultados atuais do commit apresentado.

## Cena 4 — Análise de vulnerabilidades de dependências (0:30)

Abra e apresente:

- evidência do [OWASP Dependency Track](analise-vulnerabilidades/owasp-dependency-track.png);
- imagens dos resultados do [SonarQube](analise-vulnerabilidades/sonar-qube-overall-code.png) e [sonar-qube-security-issues.png](analise-vulnerabilidades/sonar-qube-security-issues.png);
- arquivo [`docs/analise-vulnerabilidades/trivy-check.txt`](analise-vulnerabilidades/trivy-check.txt).

> Fala: "Usamos ferramentas complementares: OWASP Dependency Track para componentes, SonarQube para qualidade e segurança do código e Trivy para dependências, imagem e configuração."  

### Interpretação do resultado atual do Trivy

No arquivo de saída do Trivy:

- `pom.xml`: 0 vulnerabilidades;
- testes Dockerfile: 27, dos quais 26 passaram;
- 1 recomendação de severidade `LOW`;
- resultado identificado: `DS-0026`, falta da instrução `HEALTHCHECK` no Dockerfile.

Explique:

> Fala: "Não houve vulnerabilidade crítica, alta ou média reportada pelo resultado atual do Trivy. Existe uma melhoria de operação baixa: adicionar um health check ao contêiner."


## Cena 5 — Conclusão da análise (0:15)

> Fala: "A análise combina testes automatizados, cobertura, qualidade de código e scanners de segurança. O resultado mostra o estado atual da entrega. A única recomendação operacional visível no relatório Trivy é um health check para o Dockerfile; ela deve ser registrada como melhoria e, se corrigida, o relatório precisa ser gerado novamente."
