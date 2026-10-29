package br.com.fiap.oficina.domain.ordemservico;

public interface CadastroDeVeiculos {

    VeiculoIdentificado identificar(String cpfCnpj, String placa);
}
