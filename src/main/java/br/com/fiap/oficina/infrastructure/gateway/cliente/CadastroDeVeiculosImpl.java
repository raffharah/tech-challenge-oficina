package br.com.fiap.oficina.infrastructure.gateway.cliente;

import br.com.fiap.oficina.application.cliente.ClienteDTO;
import br.com.fiap.oficina.application.cliente.ConsultarClientes;
import br.com.fiap.oficina.application.cliente.ConsultarVeiculos;
import br.com.fiap.oficina.application.cliente.VeiculoDTO;
import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.cliente.CpfCnpj;
import br.com.fiap.oficina.domain.cliente.Placa;
import br.com.fiap.oficina.domain.ordemservico.CadastroDeVeiculos;
import br.com.fiap.oficina.domain.ordemservico.VeiculoIdentificado;
import org.springframework.stereotype.Component;

@Component
public class CadastroDeVeiculosImpl implements CadastroDeVeiculos {

    private final ConsultarClientes clientes;
    private final ConsultarVeiculos veiculos;

    CadastroDeVeiculosImpl(ConsultarClientes clientes, ConsultarVeiculos veiculos) {
        this.clientes = clientes;
        this.veiculos = veiculos;
    }

    @Override
    public VeiculoIdentificado identificar(String cpfCnpj, String placa) {
        String cpfCnpjNormalizado = new CpfCnpj(cpfCnpj).valor();
        String placaNormalizada = new Placa(placa).valor();
        ClienteDTO cliente = clientes.buscarPorCpfCnpj(cpfCnpjNormalizado);
        VeiculoDTO veiculo = veiculos.buscarPorPlaca(placaNormalizada);
        if (!veiculo.clienteId().equals(cliente.id())) {
            throw new DomainException("VEICULO_DE_OUTRO_CLIENTE", "Veiculo pertence a outro cliente");
        }
        return new VeiculoIdentificado(veiculo.id(), cpfCnpjNormalizado, placaNormalizada);
    }
}
