package br.com.fiap.oficina.infrastructure.http.estoque;

import br.com.fiap.oficina.application.estoque.AtualizarItem;
import br.com.fiap.oficina.application.estoque.AtualizarItemDTO;
import br.com.fiap.oficina.application.estoque.CadastrarItem;
import br.com.fiap.oficina.application.estoque.CadastrarItemDTO;
import br.com.fiap.oficina.application.estoque.ConsultarItens;
import br.com.fiap.oficina.application.estoque.ItemEstoqueDTO;
import br.com.fiap.oficina.application.estoque.RegistrarEntrada;
import br.com.fiap.oficina.application.estoque.RegistrarEntradaDTO;
import br.com.fiap.oficina.application.estoque.RemoverItem;
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
@RequestMapping("/itens")
@SecurityRequirement(name = "bearer")
public class ItemEstoqueController {

    private final CadastrarItem cadastrar;
    private final AtualizarItem atualizar;
    private final ConsultarItens consultar;
    private final RemoverItem remover;
    private final RegistrarEntrada registrarEntrada;

    public ItemEstoqueController(CadastrarItem cadastrar, AtualizarItem atualizar, ConsultarItens consultar,
            RemoverItem remover, RegistrarEntrada registrarEntrada) {
        this.cadastrar = cadastrar;
        this.atualizar = atualizar;
        this.consultar = consultar;
        this.remover = remover;
        this.registrarEntrada = registrarEntrada;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    ItemEstoqueDTO cadastrar(@Valid @RequestBody CadastrarItemRequest request) {
        return cadastrar.executar(new CadastrarItemDTO(request.sku(), request.nome(), request.tipo(),
                request.precoVenda(), request.saldoInicial()));
    }

    @PutMapping("/{id}")
    @Transactional
    ItemEstoqueDTO atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarItemRequest request) {
        return atualizar.executar(new AtualizarItemDTO(id, request.nome(), request.precoVenda()));
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    ItemEstoqueDTO buscarPorId(@PathVariable Long id) {
        return consultar.buscarPorId(id);
    }

    @GetMapping
    @Transactional(readOnly = true)
    Pagina<ItemEstoqueDTO> listar(@RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return consultar.listar(pagina, tamanho);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void remover(@PathVariable Long id) {
        remover.executar(id);
    }

    @PostMapping("/{id}/entradas")
    @Transactional
    ItemEstoqueDTO registrarEntrada(@PathVariable Long id, @Valid @RequestBody RegistrarEntradaRequest request) {
        return registrarEntrada.executar(new RegistrarEntradaDTO(id, request.quantidade()));
    }
}
