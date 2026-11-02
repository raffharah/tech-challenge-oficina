package br.com.fiap.oficina.application.cliente;

import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.domain.Pagina;
import br.com.fiap.oficina.domain.cliente.Placa;
import br.com.fiap.oficina.domain.cliente.VeiculoRepository;
import java.util.List;

public class ConsultarVeiculos {

    private final VeiculoRepository veiculos;

    public ConsultarVeiculos(VeiculoRepository veiculos) {
        this.veiculos = veiculos;
    }

    public VeiculoDTO buscarPorId(Long id) {
        return veiculos.buscarPorId(id).map(VeiculoDTO::de).orElseThrow(ConsultarVeiculos::naoEncontrado);
    }

    public VeiculoDTO buscarPorPlaca(String placa) {
        return veiculos.buscarPorPlaca(new Placa(placa)).map(VeiculoDTO::de)
                .orElseThrow(ConsultarVeiculos::naoEncontrado);
    }

    // placa é filtro opcional: sem ela lista todos; com ela devolve a página com 0 ou 1 veículo
    public Pagina<VeiculoDTO> listar(String placa, int pagina, int tamanho) {
        if (placa == null || placa.isBlank()) {
            return veiculos.listar(pagina, tamanho).map(VeiculoDTO::de);
        }
        List<VeiculoDTO> encontrados = veiculos.buscarPorPlaca(new Placa(placa)).map(VeiculoDTO::de).stream()
                .toList();
        return Pagina.de(encontrados, pagina, tamanho);
    }

    public List<VeiculoDTO> listarPorCliente(Long clienteId) {
        return veiculos.listarPorCliente(clienteId).stream().map(VeiculoDTO::de).toList();
    }

    private static NaoEncontradoException naoEncontrado() {
        return new NaoEncontradoException("VEICULO_NAO_ENCONTRADO", "Veiculo nao encontrado");
    }
}
