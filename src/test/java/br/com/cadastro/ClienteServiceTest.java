package br.com.cadastro;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {
  @Mock ClienteRepository repository;
  @InjectMocks ClienteService service;

  ClienteRequest request() {
    return new ClienteRequest(
        " Maria Silva ", " MARIA@example.com ", "11999998888", null, null, "sp", null, true);
  }

  @Test
  void normalizaCadastro() {
    when(repository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
    Cliente c = service.salvar(null, request());
    assertEquals("Maria Silva", c.nome);
    assertEquals("maria@example.com", c.email);
    assertEquals("SP", c.uf);
    assertTrue(c.ativo);
  }

  @Test
  void rejeitaEmailDuplicado() {
    when(repository.existsByEmailIgnoreCaseAndIdNot("maria@example.com", 0L)).thenReturn(true);
    assertEquals(
        409,
        assertThrows(ResponseStatusException.class, () -> service.salvar(null, request()))
            .getStatusCode()
            .value());
    verify(repository, never()).saveAndFlush(any());
  }

  @Test
  void naoExcluiClienteInexistente() {
    when(repository.findById(99L)).thenReturn(Optional.empty());
    assertEquals(
        404,
        assertThrows(ResponseStatusException.class, () -> service.excluir(99L))
            .getStatusCode()
            .value());
    verify(repository, never()).delete(any());
  }

  @Test
  void atualizaClienteExistente() {
    Cliente c = new Cliente();
    c.id = 4L;
    when(repository.findById(4L)).thenReturn(Optional.of(c));
    when(repository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
    assertEquals(4L, service.salvar(4L, request()).id);
    verify(repository).existsByEmailIgnoreCaseAndIdNot("maria@example.com", 4L);
  }
}
