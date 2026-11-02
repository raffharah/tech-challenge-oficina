package br.com.fiap.oficina.infrastructure.http.ordemservico;

import br.com.fiap.oficina.application.ordemservico.AtualizarServico;
import br.com.fiap.oficina.application.ordemservico.AtualizarServicoDTO;
import br.com.fiap.oficina.application.ordemservico.CadastrarServico;
import br.com.fiap.oficina.application.ordemservico.CadastrarServicoDTO;
import br.com.fiap.oficina.application.ordemservico.ConsultarServicos;
import br.com.fiap.oficina.application.ordemservico.RemoverServico;
import br.com.fiap.oficina.application.ordemservico.ServicoDTO;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Catálogo de serviços", description = """
        Serviços oferecidos pela oficina, com nome único e valor padrão (mão de obra) usado ao incluir o \
        serviço na OS.""")
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

    @Operation(summary = "Cadastrar serviço", description = """
            Cadastra o serviço no catálogo. O nome é único sem diferenciar maiúsculas e o valor padrão \
            deve ser maior que zero.""")
    @ApiResponse(responseCode = "201", description = "Serviço cadastrado.")
    @ApiResponse(responseCode = "400", description = "Campo obrigatório ausente.")
    @ApiResponse(responseCode = "409", description = "`SERVICO_DUPLICADO`: nome já cadastrado.")
    @ApiResponse(responseCode = "422", description = "`NOME_OBRIGATORIO` ou `VALOR_PADRAO_INVALIDO`.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    ServicoDTO cadastrar(@Valid @RequestBody ServicoRequest request) {
        return cadastrar.executar(new CadastrarServicoDTO(request.nome(), request.valorPadrao()));
    }

    @Operation(summary = "Atualizar serviço",
            description = "Altera nome e valor padrão. OS que já incluíram o serviço mantêm o valor copiado.")
    @ApiResponse(responseCode = "200", description = "Serviço atualizado.")
    @ApiResponse(responseCode = "400", description = "Campo obrigatório ausente.")
    @ApiResponse(responseCode = "404", description = "`SERVICO_NAO_ENCONTRADO`.")
    @ApiResponse(responseCode = "409", description = "`SERVICO_DUPLICADO`: nome já usado por outro serviço.")
    @ApiResponse(responseCode = "422", description = "`NOME_OBRIGATORIO` ou `VALOR_PADRAO_INVALIDO`.")
    @PutMapping("/{id}")
    @Transactional
    ServicoDTO atualizar(@Parameter(description = "Id do serviço.", example = "1") @PathVariable Long id,
            @Valid @RequestBody ServicoRequest request) {
        return atualizar.executar(new AtualizarServicoDTO(id, request.nome(), request.valorPadrao()));
    }

    @Operation(summary = "Buscar serviço por id", description = "Devolve o serviço do catálogo com o id informado.")
    @ApiResponse(responseCode = "200", description = "Serviço encontrado.")
    @ApiResponse(responseCode = "404", description = "`SERVICO_NAO_ENCONTRADO`.")
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    ServicoDTO buscarPorId(@Parameter(description = "Id do serviço.", example = "1") @PathVariable Long id) {
        return consultar.buscarPorId(id);
    }

    @Operation(summary = "Listar serviços", description = "Lista o catálogo de serviços em páginas.")
    @ApiResponse(responseCode = "200", description = "Página de serviços.")
    @GetMapping
    @Transactional(readOnly = true)
    Pagina<ServicoDTO> listar(@RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return consultar.listar(pagina, tamanho);
    }

    @Operation(summary = "Remover serviço",
            description = "Remove o serviço do catálogo, desde que nenhuma OS o tenha incluído.")
    @ApiResponse(responseCode = "204", description = "Serviço removido.")
    @ApiResponse(responseCode = "404", description = "`SERVICO_NAO_ENCONTRADO`.")
    @ApiResponse(responseCode = "409", description = "`CONFLITO`: serviço usado em ordem de serviço.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void remover(@Parameter(description = "Id do serviço.", example = "1") @PathVariable Long id) {
        remover.executar(id);
    }
}
