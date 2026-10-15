package br.com.cadastro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ClienteApiTest {
  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", postgres::getJdbcUrl);
    r.add("spring.datasource.username", postgres::getUsername);
    r.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;
  @Autowired ClienteRepository repository;

  @BeforeEach
  void limpar() {
    repository.deleteAll();
  }

  String body(String email) {
    return """
{"nome":"Maria Silva","email":"%s","telefone":"11999998888","cidade":"Santo André","uf":"SP","cep":"09000000","ativo":true}
"""
        .formatted(email);
  }

  @Test
  void cicloCompleto() throws Exception {
    String json =
        mvc.perform(
                post("/api/v1/clientes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body("maria@example.com")))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    long id = mapper.readTree(json).get("id").asLong();
    mvc.perform(get("/api/v1/clientes/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nome").value("Maria Silva"));
    mvc.perform(get("/api/v1/clientes").param("busca", "MARIA").param("ativo", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1));
    mvc.perform(
            put("/api/v1/clientes/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("novo@example.com").replace("true", "false")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ativo").value(false));
    mvc.perform(get("/api/v1/clientes").param("ativo", "true"))
        .andExpect(jsonPath("$.totalElements").value(0));
    mvc.perform(delete("/api/v1/clientes/" + id)).andExpect(status().isNoContent());
    mvc.perform(get("/api/v1/clientes/" + id)).andExpect(status().isNotFound());
  }

  @Test
  void validaEImpedeDuplicidade() throws Exception {
    mvc.perform(
            post("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("invalido")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erros").isArray());
    mvc.perform(
            post("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("maria@example.com")))
        .andExpect(status().isCreated());
    mvc.perform(
            post("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("MARIA@example.com")))
        .andExpect(status().isConflict());
    mvc.perform(get("/api/v1/clientes").param("size", "101")).andExpect(status().isBadRequest());
  }
}
