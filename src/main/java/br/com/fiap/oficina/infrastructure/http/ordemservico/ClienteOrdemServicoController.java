package br.com.fiap.oficina.infrastructure.http.ordemservico;

import br.com.fiap.oficina.application.ordemservico.AprovarOrcamento;
import br.com.fiap.oficina.application.ordemservico.ConsultarAndamento;
import br.com.fiap.oficina.application.ordemservico.ListarOrdensDoCliente;
import br.com.fiap.oficina.application.ordemservico.OrdemServicoDTO;
import br.com.fiap.oficina.application.ordemservico.OrdemServicoResumoDTO;
import br.com.fiap.oficina.application.ordemservico.RejeitarOrcamento;
import br.com.fiap.oficina.domain.Pagina;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cliente: acompanhamento da OS", description = """
        Rotas do cliente da oficina, sem token: ele se identifica pelo CPF/CNPJ do titular da OS. \
        OS de outro titular responde igual a OS inexistente (404), para não expor dados de terceiros.""")
@RestController
@RequestMapping("/cliente/ordens-servico")
public class ClienteOrdemServicoController {

    private final AprovarOrcamento aprovar;
    private final RejeitarOrcamento rejeitar;
    private final ConsultarAndamento consultar;
    private final ListarOrdensDoCliente listar;

    public ClienteOrdemServicoController(AprovarOrcamento aprovar, RejeitarOrcamento rejeitar,
            ConsultarAndamento consultar, ListarOrdensDoCliente listar) {
        this.aprovar = aprovar;
        this.rejeitar = rejeitar;
        this.consultar = consultar;
        this.listar = listar;
    }

    @Operation(summary = "Aprovar orçamento", description = """
            O titular aprova o orçamento: a OS vai de `AGUARDANDO_APROVACAO` para `EM_EXECUCAO` e as \
            peças e insumos são baixados do estoque. A baixa é tudo ou nada: faltando saldo de algum \
            item, nada é baixado e a OS continua aguardando aprovação.""")
    @ApiResponse(responseCode = "200", description = "OS em `EM_EXECUCAO`.")
    @ApiResponse(responseCode = "400", description = "Corpo sem `cpfCnpj`.")
    @ApiResponse(responseCode = "404", description = "`ORDEM_SERVICO_NAO_ENCONTRADA` (ou de outro titular).")
    @ApiResponse(responseCode = "422", description = """
            `SALDO_INSUFICIENTE` (com os itens em falta em `detalhes`), `TRANSICAO_INVALIDA` ou \
            `CPF_CNPJ_INVALIDO`.""")
    @PostMapping("/{id}/aprovacao")
    @Transactional
    OrdemServicoDTO aprovar(@Parameter(description = "Número da OS.", example = "1") @PathVariable Long id,
            @Valid @RequestBody DecisaoOrcamentoRequest request) {
        return aprovar.executar(id, request.cpfCnpj());
    }

    @Operation(summary = "Rejeitar orçamento", description = """
            O titular rejeita o orçamento: a OS vai de `AGUARDANDO_APROVACAO` para `CANCELADA`. \
            Nada sai do estoque.""")
    @ApiResponse(responseCode = "200", description = "OS em `CANCELADA`.")
    @ApiResponse(responseCode = "400", description = "Corpo sem `cpfCnpj`.")
    @ApiResponse(responseCode = "404", description = "`ORDEM_SERVICO_NAO_ENCONTRADA` (ou de outro titular).")
    @ApiResponse(responseCode = "422", description = "`TRANSICAO_INVALIDA` ou `CPF_CNPJ_INVALIDO`.")
    @PostMapping("/{id}/rejeicao")
    @Transactional
    OrdemServicoDTO rejeitar(@Parameter(description = "Número da OS.", example = "1") @PathVariable Long id,
            @Valid @RequestBody DecisaoOrcamentoRequest request) {
        return rejeitar.executar(id, request.cpfCnpj());
    }

    @Operation(summary = "Listar minhas OS", description = """
            Lista as OS do titular do CPF/CNPJ, da abertura mais recente para a mais antiga, com status \
            e total. Documento sem OS recebe página vazia.""")
    @ApiResponse(responseCode = "200", description = "Página com o resumo das OS do titular.")
    @ApiResponse(responseCode = "400", description = "Parâmetro `cpfCnpj` ausente.")
    @ApiResponse(responseCode = "422", description = "`CPF_CNPJ_INVALIDO`.")
    @GetMapping
    @Transactional(readOnly = true)
    Pagina<OrdemServicoResumoDTO> listar(
            @Parameter(description = "CPF ou CNPJ do titular da OS, com ou sem máscara.", example = "529.982.247-25")
            @RequestParam String cpfCnpj,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanho) {
        return listar.executar(cpfCnpj, pagina, tamanho);
    }

    @Operation(summary = "Acompanhar OS", description = """
            Devolve a OS do titular com status atual, histórico de status com data e hora, \
            componentes e total do orçamento.""")
    @ApiResponse(responseCode = "200", description = "OS do titular.")
    @ApiResponse(responseCode = "400", description = "Parâmetro `cpfCnpj` ausente.")
    @ApiResponse(responseCode = "404", description = "`ORDEM_SERVICO_NAO_ENCONTRADA` (ou de outro titular).")
    @ApiResponse(responseCode = "422", description = "`CPF_CNPJ_INVALIDO`.")
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    OrdemServicoDTO consultar(@Parameter(description = "Número da OS.", example = "1") @PathVariable Long id,
            @Parameter(description = "CPF ou CNPJ do titular da OS, com ou sem máscara.", example = "529.982.247-25")
            @RequestParam String cpfCnpj) {
        return consultar.executar(id, cpfCnpj);
    }
}
