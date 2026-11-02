package br.com.fiap.oficina.infrastructure.http.cliente;

import br.com.fiap.oficina.application.cliente.AtualizarCliente;
import br.com.fiap.oficina.application.cliente.AtualizarClienteDTO;
import br.com.fiap.oficina.application.cliente.CadastrarCliente;
import br.com.fiap.oficina.application.cliente.CadastrarClienteDTO;
import br.com.fiap.oficina.application.cliente.ClienteDTO;
import br.com.fiap.oficina.application.cliente.ConsultarClientes;
import br.com.fiap.oficina.application.cliente.RemoverCliente;
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

@Tag(name = "Clientes", description = "Cadastro dos clientes da oficina, identificados por CPF ou CNPJ.")
@RestController
@RequestMapping("/clientes")
@SecurityRequirement(name = "bearer")
public class ClienteController {

    private final CadastrarCliente cadastrar;
    private final AtualizarCliente atualizar;
    private final ConsultarClientes consultar;
    private final RemoverCliente remover;

    public ClienteController(CadastrarCliente cadastrar, AtualizarCliente atualizar, ConsultarClientes consultar,
            RemoverCliente remover) {
        this.cadastrar = cadastrar;
        this.atualizar = atualizar;
        this.consultar = consultar;
        this.remover = remover;
    }

    @Operation(summary = "Cadastrar cliente", description = """
            Cadastra o cliente com nome e CPF ou CNPJ. O documento é validado pelos dígitos verificadores \
            e gravado sem máscara; o CNPJ pode ser alfanumérico.""")
    @ApiResponse(responseCode = "201", description = "Cliente cadastrado.")
    @ApiResponse(responseCode = "400", description = "Campo obrigatório ausente.")
    @ApiResponse(responseCode = "409", description = "`CLIENTE_DUPLICADO`: CPF/CNPJ já cadastrado.")
    @ApiResponse(responseCode = "422", description = "`CPF_CNPJ_INVALIDO` ou `NOME_OBRIGATORIO`.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    ClienteDTO cadastrar(@Valid @RequestBody CadastrarClienteRequest request) {
        return cadastrar.executar(new CadastrarClienteDTO(request.nome(), request.cpfCnpj()));
    }

    @Operation(summary = "Atualizar nome do cliente",
            description = "Altera o nome. O CPF/CNPJ identifica o cliente e não muda.")
    @ApiResponse(responseCode = "200", description = "Cliente atualizado.")
    @ApiResponse(responseCode = "400", description = "Campo obrigatório ausente.")
    @ApiResponse(responseCode = "404", description = "`CLIENTE_NAO_ENCONTRADO`.")
    @ApiResponse(responseCode = "422", description = "`NOME_OBRIGATORIO`.")
    @PutMapping("/{id}")
    @Transactional
    ClienteDTO atualizar(@Parameter(description = "Id do cliente.", example = "1") @PathVariable Long id,
            @Valid @RequestBody AtualizarClienteRequest request) {
        return atualizar.executar(new AtualizarClienteDTO(id, request.nome()));
    }

    @Operation(summary = "Buscar cliente por id", description = "Devolve o cliente com o id informado.")
    @ApiResponse(responseCode = "200", description = "Cliente encontrado.")
    @ApiResponse(responseCode = "404", description = "`CLIENTE_NAO_ENCONTRADO`.")
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    ClienteDTO buscarPorId(@Parameter(description = "Id do cliente.", example = "1") @PathVariable Long id) {
        return consultar.buscarPorId(id);
    }

    @Operation(summary = "Listar clientes", description = """
            Lista os clientes em páginas, por ordem de cadastro. Com `cpfCnpj`, devolve só o cliente \
            daquele documento (página com 0 ou 1 item); o documento pode vir com ou sem máscara.""")
    @ApiResponse(responseCode = "200", description = "Página de clientes.")
    @ApiResponse(responseCode = "422", description = "`CPF_CNPJ_INVALIDO`: filtro com documento inválido.")
    @GetMapping
    @Transactional(readOnly = true)
    Pagina<ClienteDTO> listar(
            @Parameter(description = "Filtro opcional: CPF ou CNPJ, com ou sem máscara.", example = "529.982.247-25")
            @RequestParam(required = false) String cpfCnpj,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanho) {
        return consultar.listar(cpfCnpj, pagina, tamanho);
    }

    @Operation(summary = "Remover cliente", description = "Remove o cliente que não tem veículos cadastrados.")
    @ApiResponse(responseCode = "204", description = "Cliente removido.")
    @ApiResponse(responseCode = "404", description = "`CLIENTE_NAO_ENCONTRADO`.")
    @ApiResponse(responseCode = "409", description = "`CLIENTE_COM_VEICULOS`: remova os veículos antes.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void remover(@Parameter(description = "Id do cliente.", example = "1") @PathVariable Long id) {
        remover.executar(id);
    }
}
