package br.com.fiap.oficina.infrastructure.http.cliente;

import br.com.fiap.oficina.application.cliente.AtualizarCliente;
import br.com.fiap.oficina.application.cliente.AtualizarClienteDTO;
import br.com.fiap.oficina.application.cliente.CadastrarCliente;
import br.com.fiap.oficina.application.cliente.CadastrarClienteDTO;
import br.com.fiap.oficina.application.cliente.ClienteDTO;
import br.com.fiap.oficina.application.cliente.ConsultarClientes;
import br.com.fiap.oficina.application.cliente.RemoverCliente;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    ClienteDTO cadastrar(@Valid @RequestBody CadastrarClienteRequest request) {
        return cadastrar.executar(new CadastrarClienteDTO(request.nome(), request.cpfCnpj()));
    }

    @PutMapping("/{id}")
    @Transactional
    ClienteDTO atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarClienteRequest request) {
        return atualizar.executar(new AtualizarClienteDTO(id, request.nome()));
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    ClienteDTO buscarPorId(@PathVariable Long id) {
        return consultar.buscarPorId(id);
    }

    @GetMapping(params = "cpfCnpj")
    @Transactional(readOnly = true)
    ClienteDTO buscarPorCpfCnpj(@RequestParam String cpfCnpj) {
        return consultar.buscarPorCpfCnpj(cpfCnpj);
    }

    @GetMapping
    @Transactional(readOnly = true)
    Pagina<ClienteDTO> listar(@RequestParam(defaultValue = "0") int pagina,
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
