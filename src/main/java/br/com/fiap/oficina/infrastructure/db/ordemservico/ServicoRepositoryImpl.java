package br.com.fiap.oficina.infrastructure.db.ordemservico;

import br.com.fiap.oficina.domain.Pagina;
import br.com.fiap.oficina.domain.ordemservico.Servico;
import br.com.fiap.oficina.domain.ordemservico.ServicoRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class ServicoRepositoryImpl implements ServicoRepository {

    private final ServicoJpaRepository jpa;

    ServicoRepositoryImpl(ServicoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Servico salvar(Servico servico) {
        ServicoEntity entity = new ServicoEntity();
        entity.id = servico.getId();
        entity.nome = servico.getNome();
        entity.valorPadrao = servico.getValorPadrao();
        return paraModelo(jpa.saveAndFlush(entity));
    }

    @Override
    public Optional<Servico> buscarPorId(Long id) {
        return jpa.findById(id).map(ServicoRepositoryImpl::paraModelo);
    }

    @Override
    public Optional<Servico> buscarPorNome(String nome) {
        return jpa.findByNomeIgnoreCase(nome).map(ServicoRepositoryImpl::paraModelo);
    }

    @Override
    public Pagina<Servico> listar(int pagina, int tamanho) {
        Page<ServicoEntity> page = jpa.findAll(PageRequest.of(pagina, tamanho, Sort.by("id")));
        return new Pagina<>(page.map(ServicoRepositoryImpl::paraModelo).getContent(), pagina, tamanho,
                page.getTotalElements());
    }

    @Override
    public void remover(Long id) {
        jpa.deleteById(id);
        jpa.flush();
    }

    private static Servico paraModelo(ServicoEntity entity) {
        return new Servico(entity.id, entity.nome, entity.valorPadrao);
    }
}
