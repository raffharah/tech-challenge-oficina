package br.com.fiap.oficina.infrastructure.db.cliente;

import br.com.fiap.oficina.domain.Pagina;
import br.com.fiap.oficina.domain.cliente.Cliente;
import br.com.fiap.oficina.domain.cliente.ClienteRepository;
import br.com.fiap.oficina.domain.cliente.CpfCnpj;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class ClienteRepositoryImpl implements ClienteRepository {

    private final ClienteJpaRepository jpa;

    ClienteRepositoryImpl(ClienteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        ClienteEntity entity = new ClienteEntity();
        entity.id = cliente.getId();
        entity.nome = cliente.getNome();
        entity.cpfCnpj = cliente.getCpfCnpj().valor();
        return paraModelo(jpa.saveAndFlush(entity));
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return jpa.findById(id).map(ClienteRepositoryImpl::paraModelo);
    }

    @Override
    public Optional<Cliente> buscarPorCpfCnpj(CpfCnpj cpfCnpj) {
        return jpa.findByCpfCnpj(cpfCnpj.valor()).map(ClienteRepositoryImpl::paraModelo);
    }

    @Override
    public Pagina<Cliente> listar(int pagina, int tamanho) {
        Page<ClienteEntity> page = jpa.findAll(PageRequest.of(pagina, tamanho, Sort.by("id")));
        return new Pagina<>(page.map(ClienteRepositoryImpl::paraModelo).getContent(), pagina, tamanho,
                page.getTotalElements());
    }

    @Override
    public void remover(Long id) {
        jpa.deleteById(id);
        jpa.flush();
    }

    private static Cliente paraModelo(ClienteEntity entity) {
        return new Cliente(entity.id, entity.nome, new CpfCnpj(entity.cpfCnpj));
    }
}
