package br.com.fiap.oficina.infrastructure.http.ordemservico;

import br.com.fiap.oficina.application.ordemservico.AprovarOrcamento;
import br.com.fiap.oficina.application.ordemservico.OrdemServicoDTO;
import br.com.fiap.oficina.application.ordemservico.RejeitarOrcamento;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cliente/ordens-servico")
public class ClienteOrdemServicoController {

    private final AprovarOrcamento aprovar;
    private final RejeitarOrcamento rejeitar;

    public ClienteOrdemServicoController(AprovarOrcamento aprovar, RejeitarOrcamento rejeitar) {
        this.aprovar = aprovar;
        this.rejeitar = rejeitar;
    }

    @PostMapping("/{id}/aprovacao")
    @Transactional
    OrdemServicoDTO aprovar(@PathVariable Long id) {
        return aprovar.executar(id);
    }

    @PostMapping("/{id}/rejeicao")
    @Transactional
    OrdemServicoDTO rejeitar(@PathVariable Long id) {
        return rejeitar.executar(id);
    }
}
