package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.Pagina;
import br.com.fiap.oficina.domain.ordemservico.CadastroDeVeiculos;
import br.com.fiap.oficina.domain.ordemservico.OrdemServicoRepository;

// titular sem OS recebe página vazia, igual a CPF/CNPJ sem cadastro, para não revelar quem é cliente
public class ListarOrdensDoCliente {

    private final OrdemServicoRepository ordens;
    private final CadastroDeVeiculos veiculos;

    public ListarOrdensDoCliente(OrdemServicoRepository ordens, CadastroDeVeiculos veiculos) {
        this.ordens = ordens;
        this.veiculos = veiculos;
    }

    public Pagina<OrdemServicoResumoDTO> executar(String cpfCnpj, int pagina, int tamanho) {
        return ordens.listarPorCpfCnpj(veiculos.normalizarCpfCnpj(cpfCnpj), pagina, tamanho)
                .map(OrdemServicoResumoDTO::de);
    }
}
