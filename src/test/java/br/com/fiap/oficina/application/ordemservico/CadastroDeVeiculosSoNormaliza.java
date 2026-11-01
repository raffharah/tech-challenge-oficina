package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.CadastroDeVeiculos;
import br.com.fiap.oficina.domain.ordemservico.VeiculoIdentificado;

final class CadastroDeVeiculosSoNormaliza implements CadastroDeVeiculos {

    @Override
    public VeiculoIdentificado identificar(String cpfCnpj, String placa) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String normalizarCpfCnpj(String cpfCnpj) {
        return cpfCnpj.replaceAll("\\D", "");
    }
}
