package br.com.fiap.oficina.domain.ordemservico;

public interface CadastroDeVeiculos {

    VeiculoIdentificado identificar(String cpfCnpj, String placa);

    void registrarEmail(String cpfCnpj, String email);

    String normalizarCpfCnpj(String cpfCnpj);
}
