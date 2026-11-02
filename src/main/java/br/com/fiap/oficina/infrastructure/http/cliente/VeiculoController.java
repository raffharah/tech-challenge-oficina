package br.com.fiap.oficina.infrastructure.http.cliente;

import br.com.fiap.oficina.application.cliente.AtualizarVeiculo;
import br.com.fiap.oficina.application.cliente.AtualizarVeiculoDTO;
import br.com.fiap.oficina.application.cliente.CadastrarVeiculo;
import br.com.fiap.oficina.application.cliente.CadastrarVeiculoDTO;
import br.com.fiap.oficina.application.cliente.ConsultarVeiculos;
import br.com.fiap.oficina.application.cliente.RemoverVeiculo;
import br.com.fiap.oficina.application.cliente.VeiculoDTO;
import br.com.fiap.oficina.domain.Pagina;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Veículos", description = "Cadastro dos veículos, identificados pela placa e vinculados a um cliente.")
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

    @Operation(summary = "Cadastrar veículo", description = """
            Cadastra o veículo do cliente. A placa aceita o padrão antigo (`ABC1234`) e o Mercosul \
            (`ABC1D23`), com ou sem hífen, e é gravada em maiúsculas sem hífen. O ano vai de 1900 ao \
            ano seguinte ao atual.""")
    @ApiResponse(responseCode = "201", description = "Veículo cadastrado.")
    @ApiResponse(responseCode = "400", description = "Campo obrigatório ausente.")
    @ApiResponse(responseCode = "404", description = "`CLIENTE_NAO_ENCONTRADO`: `clienteId` inexistente.")
    @ApiResponse(responseCode = "409", description = "`VEICULO_DUPLICADO`: placa já cadastrada.")
    @ApiResponse(responseCode = "422", description = "`PLACA_INVALIDA`, `VEICULO_INVALIDO` ou `ANO_VEICULO_INVALIDO`.")
    @PostMapping("/veiculos")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    VeiculoDTO cadastrar(@Valid @RequestBody CadastrarVeiculoRequest request) {
        return cadastrar.executar(new CadastrarVeiculoDTO(request.placa(), request.marca(), request.modelo(),
                request.ano(), request.clienteId()));
    }

    @Operation(summary = "Atualizar veículo",
            description = "Altera marca, modelo e ano. Placa e cliente não mudam.")
    @ApiResponse(responseCode = "200", description = "Veículo atualizado.")
    @ApiResponse(responseCode = "400", description = "Campo obrigatório ausente.")
    @ApiResponse(responseCode = "404", description = "`VEICULO_NAO_ENCONTRADO`.")
    @ApiResponse(responseCode = "422", description = "`VEICULO_INVALIDO` ou `ANO_VEICULO_INVALIDO`.")
    @PutMapping("/veiculos/{id}")
    @Transactional
    VeiculoDTO atualizar(@Parameter(description = "Id do veículo.", example = "1") @PathVariable Long id,
            @Valid @RequestBody AtualizarVeiculoRequest request) {
        return atualizar.executar(new AtualizarVeiculoDTO(id, request.marca(), request.modelo(), request.ano()));
    }

    @Operation(summary = "Buscar veículo por id", description = "Devolve o veículo com o id informado.")
    @ApiResponse(responseCode = "200", description = "Veículo encontrado.")
    @ApiResponse(responseCode = "404", description = "`VEICULO_NAO_ENCONTRADO`.")
    @GetMapping("/veiculos/{id}")
    @Transactional(readOnly = true)
    VeiculoDTO buscarPorId(@Parameter(description = "Id do veículo.", example = "1") @PathVariable Long id) {
        return consultar.buscarPorId(id);
    }

    @Operation(summary = "Listar veículos", description = """
            Lista os veículos em páginas, por ordem de cadastro. Com `placa`, devolve só o veículo \
            daquela placa (página com 0 ou 1 item); a placa pode vir com hífen ou minúsculas.""")
    @ApiResponse(responseCode = "200", description = "Página de veículos.")
    @ApiResponse(responseCode = "422", description = "`PLACA_INVALIDA`: filtro com placa inválida.")
    @GetMapping("/veiculos")
    @Transactional(readOnly = true)
    Pagina<VeiculoDTO> listar(@Parameter(description = "Filtro opcional: placa, com ou sem hífen.", example = "BRA2E19")
            @RequestParam(required = false) String placa,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanho) {
        return consultar.listar(placa, pagina, tamanho);
    }

    @Operation(summary = "Listar veículos do cliente",
            description = "Devolve todos os veículos do cliente, por ordem de cadastro.")
    @ApiResponse(responseCode = "200", description = "Veículos do cliente (lista vazia se não houver).")
    @GetMapping("/clientes/{clienteId}/veiculos")
    @Transactional(readOnly = true)
    List<VeiculoDTO> listarPorCliente(
            @Parameter(description = "Id do cliente.", example = "1") @PathVariable Long clienteId) {
        return consultar.listarPorCliente(clienteId);
    }

    @Operation(summary = "Remover veículo",
            description = "Remove o veículo que não está em nenhuma ordem de serviço.")
    @ApiResponse(responseCode = "204", description = "Veículo removido.")
    @ApiResponse(responseCode = "404", description = "`VEICULO_NAO_ENCONTRADO`.")
    @ApiResponse(responseCode = "409", description = "`CONFLITO`: veículo usado em ordem de serviço.")
    @DeleteMapping("/veiculos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void remover(@Parameter(description = "Id do veículo.", example = "1") @PathVariable Long id) {
        remover.executar(id);
    }
}
