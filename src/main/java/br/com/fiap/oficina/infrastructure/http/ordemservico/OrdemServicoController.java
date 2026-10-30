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
import br.com.fiap.oficina.application.ordemservico.ListarOrdensServico;
import br.com.fiap.oficina.application.ordemservico.OrdemServicoDTO;
import br.com.fiap.oficina.application.ordemservico.OrdemServicoResumoDTO;
import br.com.fiap.oficina.application.ordemservico.RemoverItemDaOrdem;
import br.com.fiap.oficina.application.ordemservico.RemoverServicoDaOrdem;
import br.com.fiap.oficina.application.ordemservico.TempoMedioExecucaoDTO;
import br.com.fiap.oficina.domain.Pagina;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

@RestController
@RequestMapping("/ordens-servico")
@SecurityRequirement(name = "bearer")
public class OrdemServicoController {

    private final AbrirOrdemServico abrir;
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

    public OrdemServicoController(AbrirOrdemServico abrir, IncluirServicoNaOrdem incluirServico,
            RemoverServicoDaOrdem removerServico, IncluirItemNaOrdem incluirItem, RemoverItemDaOrdem removerItem,
            ConcluirDiagnostico concluirDiagnostico, FinalizarOrdemServico finalizar, EntregarVeiculo entregar,
            ListarOrdensServico listar, DetalharOrdemServico detalhar, CalcularTempoMedioExecucao tempoMedio) {
        this.abrir = abrir;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    OrdemServicoDTO abrir(@Valid @RequestBody AbrirOrdemServicoRequest request) {
        return abrir.executar(new AbrirOrdemServicoDTO(request.cpfCnpj(), request.placa()));
    }

    @PostMapping("/{id}/servicos")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    OrdemServicoDTO incluirServico(@PathVariable Long id, @Valid @RequestBody IncluirServicoRequest request) {
        return incluirServico.executar(id, request.servicoId());
    }

    @DeleteMapping("/{id}/servicos/{servicoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void removerServico(@PathVariable Long id, @PathVariable Long servicoId) {
        removerServico.executar(id, servicoId);
    }

    @PostMapping("/{id}/itens")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    OrdemServicoDTO incluirItem(@PathVariable Long id, @Valid @RequestBody IncluirItemRequest request) {
        return incluirItem.executar(id, request.itemId(), request.quantidade());
    }

    @DeleteMapping("/{id}/itens/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void removerItem(@PathVariable Long id, @PathVariable Long itemId) {
        removerItem.executar(id, itemId);
    }

    @PostMapping("/{id}/orcamento")
    @Transactional
    OrdemServicoDTO concluirDiagnostico(@PathVariable Long id) {
        return concluirDiagnostico.executar(id);
    }

    @PostMapping("/{id}/finalizacao")
    @Transactional
    OrdemServicoDTO finalizar(@PathVariable Long id) {
        return finalizar.executar(id);
    }

    @PostMapping("/{id}/entrega")
    @Transactional
    OrdemServicoDTO entregar(@PathVariable Long id) {
        return entregar.executar(id);
    }

    @GetMapping
    @Transactional(readOnly = true)
    Pagina<OrdemServicoResumoDTO> listar(@RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return listar.executar(pagina, tamanho);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    OrdemServicoDTO detalhar(@PathVariable Long id) {
        return detalhar.executar(id);
    }

    @GetMapping("/tempo-medio-execucao")
    @Transactional(readOnly = true)
    TempoMedioExecucaoDTO tempoMedioExecucao() {
        return tempoMedio.executar();
    }
}
