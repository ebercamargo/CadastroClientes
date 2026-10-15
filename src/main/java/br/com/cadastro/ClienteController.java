package br.com.cadastro;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {
  private final ClienteService service;
  private final ClienteRepository repository;

  public ClienteController(ClienteService service, ClienteRepository repository) {
    this.service = service;
    this.repository = repository;
  }

  @GetMapping
  public Page<Cliente> listar(
      @RequestParam(defaultValue = "") String busca,
      @RequestParam(required = false) Boolean ativo,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    if (page < 0 || size < 1 || size > 100 || busca.length() > 180)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paginação ou busca inválida");
    return repository.pesquisar(
        busca.strip(),
        ativo,
        PageRequest.of(page, size, Sort.by("nome").ascending().and(Sort.by("id"))));
  }

  @GetMapping("/{id}")
  public Cliente obter(@PathVariable Long id) {
    return service.obter(id);
  }

  @PostMapping
  public ResponseEntity<Cliente> criar(@Valid @RequestBody ClienteRequest r) {
    Cliente c = service.salvar(null, r);
    return ResponseEntity.created(URI.create("/api/v1/clientes/" + c.id)).body(c);
  }

  @PutMapping("/{id}")
  public Cliente editar(@PathVariable Long id, @Valid @RequestBody ClienteRequest r) {
    return service.salvar(id, r);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> excluir(@PathVariable Long id) {
    service.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
