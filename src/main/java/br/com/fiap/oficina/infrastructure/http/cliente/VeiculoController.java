package br.com.fiap.oficina.infrastructure.http.cliente;

import br.com.fiap.oficina.application.cliente.AtualizarVeiculo;
import br.com.fiap.oficina.application.cliente.AtualizarVeiculoDTO;
import br.com.fiap.oficina.application.cliente.CadastrarVeiculo;
import br.com.fiap.oficina.application.cliente.CadastrarVeiculoDTO;
import br.com.fiap.oficina.application.cliente.ConsultarVeiculos;
import br.com.fiap.oficina.application.cliente.RemoverVeiculo;
import br.com.fiap.oficina.application.cliente.VeiculoDTO;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SecurityRequirement(name = "bearer")
public class VeiculoController {

    private final CadastrarVeiculo cadastrar;
    private final AtualizarVeiculo atualizar;
    private final ConsultarVeiculos consultar;
    private final RemoverVeiculo remover;

    public VeiculoController(CadastrarVeiculo cadastrar, AtualizarVeiculo atualizar, ConsultarVeiculos consultar,
            RemoverVeiculo remover) {
        this.cadastrar = cadastrar;
        this.atualizar = atualizar;
        this.consultar = consultar;
        this.remover = remover;
    }

    @PostMapping("/veiculos")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    VeiculoDTO cadastrar(@Valid @RequestBody CadastrarVeiculoRequest request) {
        return cadastrar.executar(new CadastrarVeiculoDTO(request.placa(), request.marca(), request.modelo(),
                request.ano(), request.clienteId()));
    }

    @PutMapping("/veiculos/{id}")
    @Transactional
    VeiculoDTO atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarVeiculoRequest request) {
        return atualizar.executar(new AtualizarVeiculoDTO(id, request.marca(), request.modelo(), request.ano()));
    }

    @GetMapping("/veiculos/{id}")
    @Transactional(readOnly = true)
    VeiculoDTO buscarPorId(@PathVariable Long id) {
        return consultar.buscarPorId(id);
    }

    @GetMapping(path = "/veiculos", params = "placa")
    @Transactional(readOnly = true)
    VeiculoDTO buscarPorPlaca(@RequestParam String placa) {
        return consultar.buscarPorPlaca(placa);
    }

    @GetMapping("/clientes/{clienteId}/veiculos")
    @Transactional(readOnly = true)
    List<VeiculoDTO> listarPorCliente(@PathVariable Long clienteId) {
        return consultar.listarPorCliente(clienteId);
    }

    @DeleteMapping("/veiculos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void remover(@PathVariable Long id) {
        remover.executar(id);
    }
}
