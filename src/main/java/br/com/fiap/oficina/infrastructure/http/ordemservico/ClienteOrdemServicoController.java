package br.com.fiap.oficina.infrastructure.http.ordemservico;

import br.com.fiap.oficina.application.ordemservico.AprovarOrcamento;
import br.com.fiap.oficina.application.ordemservico.ConsultarAndamento;
import br.com.fiap.oficina.application.ordemservico.ListarOrdensDoCliente;
import br.com.fiap.oficina.application.ordemservico.OrdemServicoDTO;
import br.com.fiap.oficina.application.ordemservico.OrdemServicoResumoDTO;
import br.com.fiap.oficina.application.ordemservico.RejeitarOrcamento;
import br.com.fiap.oficina.domain.Pagina;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/{id}/aprovacao")
    @Transactional
    OrdemServicoDTO aprovar(@PathVariable Long id, @Valid @RequestBody DecisaoOrcamentoRequest request) {
        return aprovar.executar(id, request.cpfCnpj());
    }

    @PostMapping("/{id}/rejeicao")
    @Transactional
    OrdemServicoDTO rejeitar(@PathVariable Long id, @Valid @RequestBody DecisaoOrcamentoRequest request) {
        return rejeitar.executar(id, request.cpfCnpj());
    }

    @GetMapping
    @Transactional(readOnly = true)
    Pagina<OrdemServicoResumoDTO> listar(@RequestParam String cpfCnpj,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanho) {
        return listar.executar(cpfCnpj, pagina, tamanho);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    OrdemServicoDTO consultar(@PathVariable Long id, @RequestParam String cpfCnpj) {
        return consultar.executar(id, cpfCnpj);
    }
}
