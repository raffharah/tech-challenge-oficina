package br.com.fiap.oficina.infrastructure.http.ordemservico;

import br.com.fiap.oficina.application.ordemservico.AtualizarServico;
import br.com.fiap.oficina.application.ordemservico.AtualizarServicoDTO;
import br.com.fiap.oficina.application.ordemservico.CadastrarServico;
import br.com.fiap.oficina.application.ordemservico.CadastrarServicoDTO;
import br.com.fiap.oficina.application.ordemservico.ConsultarServicos;
import br.com.fiap.oficina.application.ordemservico.RemoverServico;
import br.com.fiap.oficina.application.ordemservico.ServicoDTO;
import br.com.fiap.oficina.domain.Pagina;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/servicos")
@SecurityRequirement(name = "bearer")
public class ServicoController {

    private final CadastrarServico cadastrar;
    private final AtualizarServico atualizar;
    private final ConsultarServicos consultar;
    private final RemoverServico remover;

    public ServicoController(CadastrarServico cadastrar, AtualizarServico atualizar, ConsultarServicos consultar,
            RemoverServico remover) {
        this.cadastrar = cadastrar;
        this.atualizar = atualizar;
        this.consultar = consultar;
        this.remover = remover;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    ServicoDTO cadastrar(@Valid @RequestBody ServicoRequest request) {
        return cadastrar.executar(new CadastrarServicoDTO(request.nome(), request.valorPadrao()));
    }

    @PutMapping("/{id}")
    @Transactional
    ServicoDTO atualizar(@PathVariable Long id, @Valid @RequestBody ServicoRequest request) {
        return atualizar.executar(new AtualizarServicoDTO(id, request.nome(), request.valorPadrao()));
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    ServicoDTO buscarPorId(@PathVariable Long id) {
        return consultar.buscarPorId(id);
    }

    @GetMapping
    @Transactional(readOnly = true)
    Pagina<ServicoDTO> listar(@RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return consultar.listar(pagina, tamanho);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void remover(@PathVariable Long id) {
        remover.executar(id);
    }
}
