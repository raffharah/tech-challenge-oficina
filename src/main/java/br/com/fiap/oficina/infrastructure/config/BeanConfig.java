package br.com.fiap.oficina.infrastructure.config;

import br.com.fiap.oficina.application.cliente.AtualizarCliente;
import br.com.fiap.oficina.application.cliente.AtualizarVeiculo;
import br.com.fiap.oficina.application.cliente.CadastrarCliente;
import br.com.fiap.oficina.application.cliente.CadastrarVeiculo;
import br.com.fiap.oficina.application.cliente.ConsultarClientes;
import br.com.fiap.oficina.application.cliente.ConsultarVeiculos;
import br.com.fiap.oficina.application.cliente.RemoverCliente;
import br.com.fiap.oficina.application.cliente.RemoverVeiculo;
import br.com.fiap.oficina.application.ordemservico.AtualizarServico;
import br.com.fiap.oficina.application.ordemservico.CadastrarServico;
import br.com.fiap.oficina.application.ordemservico.ConsultarServicos;
import br.com.fiap.oficina.application.ordemservico.RemoverServico;
import br.com.fiap.oficina.domain.cliente.ClienteRepository;
import br.com.fiap.oficina.domain.cliente.VeiculoRepository;
import br.com.fiap.oficina.domain.ordemservico.ServicoRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    CadastrarCliente cadastrarCliente(ClienteRepository clientes) {
        return new CadastrarCliente(clientes);
    }

    @Bean
    AtualizarCliente atualizarCliente(ClienteRepository clientes) {
        return new AtualizarCliente(clientes);
    }

    @Bean
    ConsultarClientes consultarClientes(ClienteRepository clientes) {
        return new ConsultarClientes(clientes);
    }

    @Bean
    RemoverCliente removerCliente(ClienteRepository clientes, VeiculoRepository veiculos) {
        return new RemoverCliente(clientes, veiculos);
    }

    @Bean
    CadastrarVeiculo cadastrarVeiculo(VeiculoRepository veiculos, ClienteRepository clientes, Clock clock) {
        return new CadastrarVeiculo(veiculos, clientes, clock);
    }

    @Bean
    AtualizarVeiculo atualizarVeiculo(VeiculoRepository veiculos, Clock clock) {
        return new AtualizarVeiculo(veiculos, clock);
    }

    @Bean
    ConsultarVeiculos consultarVeiculos(VeiculoRepository veiculos) {
        return new ConsultarVeiculos(veiculos);
    }

    @Bean
    RemoverVeiculo removerVeiculo(VeiculoRepository veiculos) {
        return new RemoverVeiculo(veiculos);
    }

    @Bean
    CadastrarServico cadastrarServico(ServicoRepository servicos) {
        return new CadastrarServico(servicos);
    }

    @Bean
    AtualizarServico atualizarServico(ServicoRepository servicos) {
        return new AtualizarServico(servicos);
    }

    @Bean
    ConsultarServicos consultarServicos(ServicoRepository servicos) {
        return new ConsultarServicos(servicos);
    }

    @Bean
    RemoverServico removerServico(ServicoRepository servicos) {
        return new RemoverServico(servicos);
    }
}
