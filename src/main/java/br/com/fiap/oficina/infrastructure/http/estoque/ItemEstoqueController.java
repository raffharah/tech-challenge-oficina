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

@Tag(name = "Estoque de peças e insumos", description = """
        Itens de estoque (peças e insumos) com saldo físico. O saldo muda só por entrada manual e pela \
        baixa na aprovação do orçamento da OS, e nunca fica negativo.""")
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

    @Operation(summary = "Cadastrar item", description = """
            Cadastra a peça ou insumo com SKU único (sem diferenciar maiúsculas), preço de venda maior \
            que zero e saldo inicial.""")
    @ApiResponse(responseCode = "201", description = "Item cadastrado.")
    @ApiResponse(responseCode = "400", description = "Campo obrigatório ausente ou `tipo` fora de `PECA`/`INSUMO`.")
    @ApiResponse(responseCode = "409", description = "`SKU_DUPLICADO`.")
    @ApiResponse(responseCode = "422", description = """
            `SKU_OBRIGATORIO`, `NOME_OBRIGATORIO`, `TIPO_OBRIGATORIO`, `PRECO_VENDA_INVALIDO` ou \
            `SALDO_INICIAL_INVALIDO`.""")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    ItemEstoqueDTO cadastrar(@Valid @RequestBody CadastrarItemRequest request) {
        return cadastrar.executar(new CadastrarItemDTO(request.sku(), request.nome(), request.tipo(),
                request.precoVenda(), request.saldoInicial()));
    }

    @Operation(summary = "Atualizar item",
            description = "Altera nome e preço de venda. SKU, tipo e saldo não mudam por aqui.")
    @ApiResponse(responseCode = "200", description = "Item atualizado.")
    @ApiResponse(responseCode = "400", description = "Campo obrigatório ausente.")
    @ApiResponse(responseCode = "404", description = "`ITEM_NAO_ENCONTRADO`.")
    @ApiResponse(responseCode = "422", description = "`NOME_OBRIGATORIO` ou `PRECO_VENDA_INVALIDO`.")
    @PutMapping("/{id}")
    @Transactional
    ItemEstoqueDTO atualizar(@Parameter(description = "Id do item.", example = "1") @PathVariable Long id,
            @Valid @RequestBody AtualizarItemRequest request) {
        return atualizar.executar(new AtualizarItemDTO(id, request.nome(), request.precoVenda()));
    }

    @Operation(summary = "Buscar item por id", description = "Devolve o item com o saldo físico atual.")
    @ApiResponse(responseCode = "200", description = "Item encontrado.")
    @ApiResponse(responseCode = "404", description = "`ITEM_NAO_ENCONTRADO`.")
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    ItemEstoqueDTO buscarPorId(@Parameter(description = "Id do item.", example = "1") @PathVariable Long id) {
        return consultar.buscarPorId(id);
    }

    @Operation(summary = "Listar itens", description = "Lista as peças e insumos em páginas, com o saldo físico.")
    @ApiResponse(responseCode = "200", description = "Página de itens.")
    @GetMapping
    @Transactional(readOnly = true)
    Pagina<ItemEstoqueDTO> listar(@RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return consultar.listar(pagina, tamanho);
    }

    @Operation(summary = "Remover item",
            description = "Remove o item do estoque, desde que nenhuma OS o tenha incluído.")
    @ApiResponse(responseCode = "204", description = "Item removido.")
    @ApiResponse(responseCode = "404", description = "`ITEM_NAO_ENCONTRADO`.")
    @ApiResponse(responseCode = "409", description = "`CONFLITO`: item usado em ordem de serviço.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void remover(@Parameter(description = "Id do item.", example = "1") @PathVariable Long id) {
        remover.executar(id);
    }

    @Operation(summary = "Registrar entrada de estoque",
            description = "Soma a quantidade recebida ao saldo físico do item (entrada manual de mercadoria).")
    @ApiResponse(responseCode = "200", description = "Item com o saldo atualizado.")
    @ApiResponse(responseCode = "400", description = "Campo obrigatório ausente.")
    @ApiResponse(responseCode = "404", description = "`ITEM_NAO_ENCONTRADO`.")
    @ApiResponse(responseCode = "422", description = "`QUANTIDADE_INVALIDA`: a quantidade deve ser maior que zero.")
    @PostMapping("/{id}/entradas")
    @Transactional
    ItemEstoqueDTO registrarEntrada(@Parameter(description = "Id do item.", example = "1") @PathVariable Long id,
            @Valid @RequestBody RegistrarEntradaRequest request) {
        return registrarEntrada.executar(new RegistrarEntradaDTO(id, request.quantidade()));
    }
}
