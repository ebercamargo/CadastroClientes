package br.com.cadastro;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {
  @Mock ClienteService service;
  @Mock ClienteRepository repository;
  MockMvc mvc;

  @BeforeEach
  void configurar() {
    mvc =
        MockMvcBuilders.standaloneSetup(new ClienteController(service, repository))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
  }

  @Test
  void retornaLocationAoCriar() throws Exception {
    Cliente cliente = new Cliente();
    cliente.id = 7L;
    cliente.nome = "Maria Silva";
    when(service.salvar(isNull(), any())).thenReturn(cliente);
    mvc.perform(
            post("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
{"nome":"Maria Silva","email":"maria@example.com","telefone":"11999998888","ativo":true}
"""))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/v1/clientes/7"))
        .andExpect(jsonPath("$.nome").value("Maria Silva"));
  }

  @Test
  void rejeitaCamposInvalidos() throws Exception {
    mvc.perform(
            post("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
{"nome":"","email":"invalido","telefone":"abc","uf":"XX","cep":"1","ativo":true}
"""))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erros").isArray());
  }

  @Test
  void rejeitaPaginacaoInvalida() throws Exception {
    mvc.perform(get("/api/v1/clientes").param("size", "101")).andExpect(status().isBadRequest());
    mvc.perform(get("/api/v1/clientes").param("page", "-1")).andExpect(status().isBadRequest());
  }

  @Test
  void excluiSemConteudo() throws Exception {
    mvc.perform(delete("/api/v1/clientes/7")).andExpect(status().isNoContent());
  }
}
