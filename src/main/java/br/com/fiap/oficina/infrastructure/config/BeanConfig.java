package br.com.fiap.oficina.infrastructure.config;

import br.com.fiap.oficina.application.cliente.AtualizarCliente;
import br.com.fiap.oficina.application.cliente.AtualizarVeiculo;
import br.com.fiap.oficina.application.cliente.CadastrarCliente;
import br.com.fiap.oficina.application.cliente.CadastrarVeiculo;
import br.com.fiap.oficina.application.cliente.ConsultarClientes;
import br.com.fiap.oficina.application.cliente.ConsultarVeiculos;
import br.com.fiap.oficina.application.cliente.RemoverCliente;
import br.com.fiap.oficina.application.cliente.RemoverVeiculo;
import br.com.fiap.oficina.application.estoque.AtualizarItem;
import br.com.fiap.oficina.application.estoque.BaixarItens;
import br.com.fiap.oficina.application.estoque.CadastrarItem;
import br.com.fiap.oficina.application.estoque.ConsultarItens;
import br.com.fiap.oficina.application.estoque.RegistrarEntrada;
import br.com.fiap.oficina.application.estoque.RemoverItem;
import br.com.fiap.oficina.application.ordemservico.AbrirOrdemServico;
import br.com.fiap.oficina.application.ordemservico.AprovarOrcamento;
import br.com.fiap.oficina.application.ordemservico.AtualizarServico;
import br.com.fiap.oficina.application.ordemservico.CadastrarServico;
import br.com.fiap.oficina.application.ordemservico.ConcluirDiagnostico;
import br.com.fiap.oficina.application.ordemservico.ConsultarServicos;
import br.com.fiap.oficina.application.ordemservico.EntregarVeiculo;
import br.com.fiap.oficina.application.ordemservico.FinalizarOrdemServico;
import br.com.fiap.oficina.application.ordemservico.IncluirItemNaOrdem;
import br.com.fiap.oficina.application.ordemservico.IncluirServicoNaOrdem;
import br.com.fiap.oficina.application.ordemservico.RejeitarOrcamento;
import br.com.fiap.oficina.application.ordemservico.RemoverItemDaOrdem;
import br.com.fiap.oficina.application.ordemservico.RemoverServicoDaOrdem;
import br.com.fiap.oficina.application.ordemservico.RemoverServico;
import br.com.fiap.oficina.domain.cliente.ClienteRepository;
import br.com.fiap.oficina.domain.cliente.VeiculoRepository;
import br.com.fiap.oficina.domain.estoque.ItemEstoqueRepository;
import br.com.fiap.oficina.domain.ordemservico.CadastroDeVeiculos;
import br.com.fiap.oficina.domain.ordemservico.EstoqueDeItens;
import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;
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

    @Bean
    CadastrarItem cadastrarItem(ItemEstoqueRepository itens) {
        return new CadastrarItem(itens);
    }

    @Bean
    AtualizarItem atualizarItem(ItemEstoqueRepository itens) {
        return new AtualizarItem(itens);
    }

    @Bean
    ConsultarItens consultarItens(ItemEstoqueRepository itens) {
        return new ConsultarItens(itens);
    }

    @Bean
    RemoverItem removerItem(ItemEstoqueRepository itens) {
        return new RemoverItem(itens);
    }

    @Bean
    BaixarItens baixarItens(ItemEstoqueRepository itens) {
        return new BaixarItens(itens);
    }

    @Bean
    RegistrarEntrada registrarEntrada(ItemEstoqueRepository itens) {
        return new RegistrarEntrada(itens);
    }

    @Bean
    AbrirOrdemServico abrirOrdemServico(OrdemServicoRepository ordens, CadastroDeVeiculos veiculos, Clock clock) {
        return new AbrirOrdemServico(ordens, veiculos, clock);
    }

    @Bean
    IncluirServicoNaOrdem incluirServicoNaOrdem(OrdemServicoRepository ordens, ServicoRepository servicos) {
        return new IncluirServicoNaOrdem(ordens, servicos);
    }

    @Bean
    RemoverServicoDaOrdem removerServicoDaOrdem(OrdemServicoRepository ordens) {
        return new RemoverServicoDaOrdem(ordens);
    }

    @Bean
    IncluirItemNaOrdem incluirItemNaOrdem(OrdemServicoRepository ordens, EstoqueDeItens estoque) {
        return new IncluirItemNaOrdem(ordens, estoque);
    }

    @Bean
    RemoverItemDaOrdem removerItemDaOrdem(OrdemServicoRepository ordens) {
        return new RemoverItemDaOrdem(ordens);
    }

    @Bean
    ConcluirDiagnostico concluirDiagnostico(OrdemServicoRepository ordens, Clock clock) {
        return new ConcluirDiagnostico(ordens, clock);
    }

    @Bean
    AprovarOrcamento aprovarOrcamento(OrdemServicoRepository ordens, EstoqueDeItens estoque, Clock clock) {
        return new AprovarOrcamento(ordens, estoque, clock);
    }

    @Bean
    RejeitarOrcamento rejeitarOrcamento(OrdemServicoRepository ordens, Clock clock) {
        return new RejeitarOrcamento(ordens, clock);
    }

    @Bean
    FinalizarOrdemServico finalizarOrdemServico(OrdemServicoRepository ordens, Clock clock) {
        return new FinalizarOrdemServico(ordens, clock);
    }

    @Bean
    EntregarVeiculo entregarVeiculo(OrdemServicoRepository ordens, Clock clock) {
        return new EntregarVeiculo(ordens, clock);
    }
}
