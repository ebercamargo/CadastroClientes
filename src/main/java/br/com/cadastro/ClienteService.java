package br.com.cadastro;

import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ClienteService {
  private final ClienteRepository repository;

  public ClienteService(ClienteRepository repository) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  public Cliente obter(Long id) {
    return repository
        .findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
  }

  public Cliente salvar(Long id, ClienteRequest r) {
    Cliente c = id == null ? new Cliente() : obter(id);
    String email = r.email().strip().toLowerCase(Locale.ROOT);
    if (repository.existsByEmailIgnoreCaseAndIdNot(email, id == null ? 0L : id))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
    c.nome = r.nome().strip();
    c.email = email;
    c.telefone = r.telefone().strip();
    c.endereco = limpar(r.endereco());
    c.cidade = limpar(r.cidade());
    c.uf = r.uf() == null ? null : r.uf().toUpperCase(Locale.ROOT);
    c.cep = r.cep();
    c.ativo = r.ativo();
    return repository.saveAndFlush(c);
  }

  public void excluir(Long id) {
    repository.delete(obter(id));
  }

  private String limpar(String valor) {
    return valor == null ? null : valor.strip();
  }
}
