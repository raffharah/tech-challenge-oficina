package br.com.fiap.oficina.infrastructure.config;

import br.com.fiap.oficina.infrastructure.http.ErroResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Oficina Mecânica API", version = "v1", description = """
        Ordens de serviço (OS), clientes, veículos, catálogo de serviços e estoque de peças e insumos da oficina.

        **Autenticação:** as rotas da oficina exigem JWT. Gere o token em `POST /autenticacao` \
        (usuário `funcionario`, senha `oficina123` no ambiente local), clique em **Authorize** e cole o token. \
        As rotas `/cliente/**` são do cliente da oficina e não usam token: ele se identifica pelo CPF/CNPJ do titular.

        **Ciclo da OS:** `RECEBIDA` → `EM_DIAGNOSTICO` → `AGUARDANDO_APROVACAO` → `EM_EXECUCAO` → `FINALIZADA` → \
        `ENTREGUE`, ou `CANCELADA` quando o cliente rejeita o orçamento. Cada transição tem a sua rota; \
        uma transição fora da sequência responde `422 TRANSICAO_INVALIDA`.

        **Erros:** todo erro tem o corpo `ErroResponse`, com um `codigo` estável para tratamento \
        (ex.: `CPF_CNPJ_INVALIDO`, `SALDO_INSUFICIENTE`). 400 indica requisição malformada, 404 recurso \
        inexistente, 409 conflito com dado existente e 422 regra de negócio violada."""))
@SecurityScheme(name = "bearer", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {

    private static final String ERRO_RESPONSE = "#/components/schemas/ErroResponse";

    // completa o que se repete em toda operação, para os controllers declararem só o específico de cada rota
    @Bean
    OpenApiCustomizer padroesDaApi() {
        return openApi -> {
            ModelConverters.getInstance().readAll(ErroResponse.class)
                    .forEach((nome, schema) -> openApi.getComponents().addSchemas(nome, schema));
            openApi.getPaths().values().forEach(path -> path.readOperations().forEach(operacao -> {
                exigirTokenResponde401(operacao);
                erroUsaErroResponse(operacao);
                documentarPaginacao(operacao);
            }));
        };
    }

    private static void exigirTokenResponde401(Operation operacao) {
        boolean exigeToken = operacao.getSecurity() != null
                && operacao.getSecurity().stream().anyMatch(requisito -> requisito.containsKey("bearer"));
        if (exigeToken && !operacao.getResponses().containsKey("401")) {
            operacao.getResponses().addApiResponse("401",
                    new ApiResponse().description("Token JWT ausente, inválido ou expirado."));
        }
    }

    // o springdoc repete o tipo de retorno do método em toda resposta declarada; erro tem corpo próprio e 401 vem vazio
    private static void erroUsaErroResponse(Operation operacao) {
        operacao.getResponses().forEach((codigo, resposta) -> {
            if (codigo.equals("401")) {
                resposta.setContent(null);
            } else if (codigo.startsWith("4")) {
                resposta.setContent(new Content().addMediaType("application/json",
                        new MediaType().schema(new Schema<>().$ref(ERRO_RESPONSE))));
            }
        });
    }

    private static void documentarPaginacao(Operation operacao) {
        if (operacao.getParameters() == null) {
            return;
        }
        for (Parameter parametro : operacao.getParameters()) {
            if (parametro.getName().equals("pagina") && parametro.getDescription() == null) {
                parametro.description("Número da página, começando em 0.").example(0);
            }
            if (parametro.getName().equals("tamanho") && parametro.getDescription() == null) {
                parametro.description("Quantidade de registros por página.").example(20);
            }
        }
    }
}
