package br.com.fiap.oficina.infrastructure.http.ordemservico;

import br.com.fiap.oficina.application.ordemservico.AbrirOrdemServico;
import br.com.fiap.oficina.application.ordemservico.AbrirOrdemServicoDTO;
import br.com.fiap.oficina.application.ordemservico.CalcularTempoMedioExecucao;
import br.com.fiap.oficina.application.ordemservico.ConcluirDiagnostico;
import br.com.fiap.oficina.application.ordemservico.DetalharOrdemServico;
import br.com.fiap.oficina.application.ordemservico.EntregarVeiculo;
import br.com.fiap.oficina.application.ordemservico.FinalizarOrdemServico;
import br.com.fiap.oficina.application.ordemservico.IncluirItemNaOrdem;
import br.com.fiap.oficina.application.ordemservico.IncluirServicoNaOrdem;
import br.com.fiap.oficina.application.ordemservico.IniciarDiagnostico;
import br.com.fiap.oficina.application.ordemservico.ListarOrdensServico;
import br.com.fiap.oficina.application.ordemservico.OrdemServicoDTO;
import br.com.fiap.oficina.application.ordemservico.OrdemServicoResumoDTO;
import br.com.fiap.oficina.application.ordemservico.RemoverItemDaOrdem;
import br.com.fiap.oficina.application.ordemservico.RemoverServicoDaOrdem;
import br.com.fiap.oficina.application.ordemservico.TempoMedioExecucaoDTO;
import br.com.fiap.oficina.domain.Pagina;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Ordens de serviço", description = """
        Rotas da oficina para conduzir a OS: abertura, diagnóstico (serviços, peças e insumos), orçamento, \
        finalização e entrega, além da listagem e do tempo médio de execução.""")
@RestController
@RequestMapping("/ordens-servico")
@SecurityRequirement(name = "bearer")
public class OrdemServicoController {

    private final AbrirOrdemServico abrir;
    private final IniciarDiagnostico iniciarDiagnostico;
    private final IncluirServicoNaOrdem incluirServico;
    private final RemoverServicoDaOrdem removerServico;
    private final IncluirItemNaOrdem incluirItem;
    private final RemoverItemDaOrdem removerItem;
    private final ConcluirDiagnostico concluirDiagnostico;
    private final FinalizarOrdemServico finalizar;
    private final EntregarVeiculo entregar;
    private final ListarOrdensServico listar;
    private final DetalharOrdemServico detalhar;
    private final CalcularTempoMedioExecucao tempoMedio;

    public OrdemServicoController(AbrirOrdemServico abrir, IniciarDiagnostico iniciarDiagnostico,
            IncluirServicoNaOrdem incluirServico,
            RemoverServicoDaOrdem removerServico, IncluirItemNaOrdem incluirItem, RemoverItemDaOrdem removerItem,
            ConcluirDiagnostico concluirDiagnostico, FinalizarOrdemServico finalizar, EntregarVeiculo entregar,
            ListarOrdensServico listar, DetalharOrdemServico detalhar, CalcularTempoMedioExecucao tempoMedio) {
        this.abrir = abrir;
        this.iniciarDiagnostico = iniciarDiagnostico;
        this.incluirServico = incluirServico;
        this.removerServico = removerServico;
        this.incluirItem = incluirItem;
        this.removerItem = removerItem;
        this.concluirDiagnostico = concluirDiagnostico;
        this.finalizar = finalizar;
        this.entregar = entregar;
        this.listar = listar;
        this.detalhar = detalhar;
        this.tempoMedio = tempoMedio;
    }

    @Operation(summary = "Abrir OS", description = """
            Abre a OS para o veículo do cliente, identificados pelo CPF/CNPJ e pela placa já \
            cadastrados. A OS nasce `RECEBIDA`; o diagnóstico começa em `POST /ordens-servico/{id}/diagnostico`.""")
    @ApiResponse(responseCode = "201", description = "OS aberta com status `RECEBIDA`.")
    @ApiResponse(responseCode = "400", description = "Campo obrigatório ausente.")
    @ApiResponse(responseCode = "404", description = "`CLIENTE_NAO_ENCONTRADO` ou `VEICULO_NAO_ENCONTRADO`.")
    @ApiResponse(responseCode = "422",
            description = "`CPF_CNPJ_INVALIDO`, `PLACA_INVALIDA` ou `VEICULO_DE_OUTRO_CLIENTE`.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    OrdemServicoDTO abrir(@Valid @RequestBody AbrirOrdemServicoRequest request) {
        return abrir.executar(new AbrirOrdemServicoDTO(request.cpfCnpj(), request.placa()));
    }

    @Operation(summary = "Iniciar diagnóstico", description = """
            O mecânico começa a avaliar o veículo: a OS vai de `RECEBIDA` para `EM_DIAGNOSTICO`. \
            Só a partir daqui serviços, peças e insumos podem ser incluídos.""")
    @ApiResponse(responseCode = "200", description = "OS em `EM_DIAGNOSTICO`.")
    @ApiResponse(responseCode = "404", description = "`ORDEM_SERVICO_NAO_ENCONTRADA`.")
    @ApiResponse(responseCode = "422", description = "`TRANSICAO_INVALIDA`: a OS não está `RECEBIDA`.")
    @PostMapping("/{id}/diagnostico")
    @Transactional
    OrdemServicoDTO iniciarDiagnostico(@Parameter(description = "Número da OS.", example = "1") @PathVariable Long id) {
        return iniciarDiagnostico.executar(id);
    }

    @Operation(summary = "Incluir serviço na OS", description = """
            Inclui um serviço do catálogo na OS em diagnóstico. A OS copia o valor padrão do serviço \
            nesse momento; mudar o catálogo depois não altera a OS.""")
    @ApiResponse(responseCode = "201", description = "Serviço incluído; devolve a OS atualizada.")
    @ApiResponse(responseCode = "404", description = "`ORDEM_SERVICO_NAO_ENCONTRADA`.")
    @ApiResponse(responseCode = "422", description = """
            `ORDEM_FORA_DE_DIAGNOSTICO`, `SERVICO_JA_INCLUIDO` ou `SERVICO_NAO_ENCONTRADO`.""")
    @PostMapping("/{id}/servicos")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    OrdemServicoDTO incluirServico(@Parameter(description = "Número da OS.", example = "1") @PathVariable Long id,
            @Valid @RequestBody IncluirServicoRequest request) {
        return incluirServico.executar(id, request.servicoId());
    }

    @Operation(summary = "Remover serviço da OS", description = "Remove o serviço da OS em diagnóstico.")
    @ApiResponse(responseCode = "204", description = "Serviço removido.")
    @ApiResponse(responseCode = "404", description = "`ORDEM_SERVICO_NAO_ENCONTRADA`.")
    @ApiResponse(responseCode = "422", description = "`ORDEM_FORA_DE_DIAGNOSTICO`.")
    @DeleteMapping("/{id}/servicos/{servicoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void removerServico(@Parameter(description = "Número da OS.", example = "1") @PathVariable Long id,
            @Parameter(description = "Id do serviço no catálogo.", example = "1")
            @PathVariable Long servicoId) {
        removerServico.executar(id, servicoId);
    }

    @Operation(summary = "Incluir peça ou insumo na OS", description = """
            Inclui um item do estoque na OS em diagnóstico, com o preço de venda atual. Incluir de novo \
            o mesmo item soma a quantidade. O estoque só é baixado na aprovação do orçamento.""")
    @ApiResponse(responseCode = "201", description = "Item incluído; devolve a OS atualizada.")
    @ApiResponse(responseCode = "404", description = "`ORDEM_SERVICO_NAO_ENCONTRADA`.")
    @ApiResponse(responseCode = "422",
            description = "`ORDEM_FORA_DE_DIAGNOSTICO`, `QUANTIDADE_INVALIDA` ou `ITEM_NAO_ENCONTRADO`.")
    @PostMapping("/{id}/itens")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    OrdemServicoDTO incluirItem(@Parameter(description = "Número da OS.", example = "1") @PathVariable Long id,
            @Valid @RequestBody IncluirItemRequest request) {
        return incluirItem.executar(id, request.itemId(), request.quantidade());
    }

    @Operation(summary = "Remover peça ou insumo da OS", description = "Remove o item da OS em diagnóstico.")
    @ApiResponse(responseCode = "204", description = "Item removido.")
    @ApiResponse(responseCode = "404", description = "`ORDEM_SERVICO_NAO_ENCONTRADA`.")
    @ApiResponse(responseCode = "422", description = "`ORDEM_FORA_DE_DIAGNOSTICO`.")
    @DeleteMapping("/{id}/itens/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void removerItem(@Parameter(description = "Número da OS.", example = "1") @PathVariable Long id,
            @Parameter(description = "Id do item de estoque.", example = "1")
            @PathVariable Long itemId) {
        removerItem.executar(id, itemId);
    }

    @Operation(summary = "Gerar orçamento", description = """
            Conclui o diagnóstico: calcula o total (serviços + peças e insumos) e envia a OS para \
            `AGUARDANDO_APROVACAO`, à espera da decisão do cliente.""")
    @ApiResponse(responseCode = "200", description = "OS em `AGUARDANDO_APROVACAO` com o `total`.")
    @ApiResponse(responseCode = "404", description = "`ORDEM_SERVICO_NAO_ENCONTRADA`.")
    @ApiResponse(responseCode = "422",
            description = "`ORDEM_FORA_DE_DIAGNOSTICO` ou `ORDEM_SEM_SERVICO`: inclua ao menos um serviço.")
    @PostMapping("/{id}/orcamento")
    @Transactional
    OrdemServicoDTO concluirDiagnostico(
            @Parameter(description = "Número da OS.", example = "1") @PathVariable Long id) {
        return concluirDiagnostico.executar(id);
    }

    @Operation(summary = "Finalizar OS",
            description = "Registra a conclusão dos serviços: a OS vai de `EM_EXECUCAO` para `FINALIZADA`.")
    @ApiResponse(responseCode = "200", description = "OS em `FINALIZADA`.")
    @ApiResponse(responseCode = "404", description = "`ORDEM_SERVICO_NAO_ENCONTRADA`.")
    @ApiResponse(responseCode = "422", description = "`TRANSICAO_INVALIDA`: a OS não está `EM_EXECUCAO`.")
    @PostMapping("/{id}/finalizacao")
    @Transactional
    OrdemServicoDTO finalizar(@Parameter(description = "Número da OS.", example = "1") @PathVariable Long id) {
        return finalizar.executar(id);
    }

    @Operation(summary = "Entregar veículo",
            description = "Registra a entrega do veículo ao cliente: a OS vai de `FINALIZADA` para `ENTREGUE`.")
    @ApiResponse(responseCode = "200", description = "OS em `ENTREGUE`.")
    @ApiResponse(responseCode = "404", description = "`ORDEM_SERVICO_NAO_ENCONTRADA`.")
    @ApiResponse(responseCode = "422", description = "`TRANSICAO_INVALIDA`: a OS não está `FINALIZADA`.")
    @PostMapping("/{id}/entrega")
    @Transactional
    OrdemServicoDTO entregar(@Parameter(description = "Número da OS.", example = "1") @PathVariable Long id) {
        return entregar.executar(id);
    }

    @Operation(summary = "Listar OS",
            description = "Lista todas as OS em páginas, da abertura mais recente para a mais antiga.")
    @ApiResponse(responseCode = "200", description = "Página com o resumo das OS.")
    @GetMapping
    @Transactional(readOnly = true)
    Pagina<OrdemServicoResumoDTO> listar(@RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return listar.executar(pagina, tamanho);
    }

    @Operation(summary = "Detalhar OS",
            description = "Devolve a OS com serviços, peças e insumos, total e histórico de status com data e hora.")
    @ApiResponse(responseCode = "200", description = "OS detalhada.")
    @ApiResponse(responseCode = "404", description = "`ORDEM_SERVICO_NAO_ENCONTRADA`.")
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    OrdemServicoDTO detalhar(@Parameter(description = "Número da OS.", example = "1") @PathVariable Long id) {
        return detalhar.executar(id);
    }

    @Operation(summary = "Tempo médio de execução", description = """
            Média, em minutos, do tempo entre a aprovação (`EM_EXECUCAO`) e a finalização (`FINALIZADA`) \
            das OS já finalizadas ou entregues. Sem OS finalizada, `mediaMinutos` vem nulo.""")
    @ApiResponse(responseCode = "200", description = "Média e quantidade de OS consideradas.")
    @GetMapping("/tempo-medio-execucao")
    @Transactional(readOnly = true)
    TempoMedioExecucaoDTO tempoMedioExecucao() {
        return tempoMedio.executar();
    }
}
