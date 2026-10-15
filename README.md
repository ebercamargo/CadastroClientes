# Cadastro de Clientes

Aplicação para gerenciar clientes, com interface web e API REST em Java e Spring Boot. Os dados são armazenados em PostgreSQL e as alterações de esquema são versionadas com Flyway.

## Funcionalidades

- Cadastro, consulta, edição e exclusão de clientes.
- Nome, e-mail, telefone, endereço, cidade, UF, CEP e situação do cadastro.
- Busca por nome ou e-mail, filtro por situação e paginação.
- Validação de campos, e-mail único sem diferenciar maiúsculas e minúsculas e respostas de erro no formato Problem Details.
- Interface responsiva em português, sem dependências de frontend.
- Testes unitários, testes de integração com PostgreSQL real via Testcontainers e verificação automática no GitHub Actions.

## Tecnologias

Java 25 LTS, Spring Boot 3.5, Spring MVC, Spring Data JPA, Bean Validation, PostgreSQL 17, Flyway, Maven, JUnit 5, Mockito e Testcontainers.

## Executar localmente

Pré-requisitos: JDK 25, Maven 3.9+ e Docker com Compose.

```bash
git clone https://github.com/ebercamargo/CadastroClientes.git
cd CadastroClientes
docker compose up -d
mvn spring-boot:run
```

Abra http://localhost:8080. O banco inicia vazio; clique em **Novo cliente** para cadastrar o primeiro contato.

A aplicação e o PostgreSQL escutam somente na interface local por padrão. O sistema não possui autenticação e foi configurado para uso local.

Configuração opcional por variáveis de ambiente:

| Variável         | Padrão                                               |
| ---------------- | ---------------------------------------------------- |
| `DB_URL`         | `jdbc:postgresql://localhost:5432/cadastro_clientes` |
| `DB_USER`        | `cadastro`                                           |
| `DB_PASSWORD`    | `cadastro_local`                                     |
| `SERVER_ADDRESS` | `127.0.0.1`                                          |

Ao alterar `DB_PASSWORD`, forneça o mesmo valor ao Compose e à aplicação. Em um volume existente, alterar a variável não muda a senha já cadastrada no PostgreSQL.

Para parar o banco sem apagar os dados: `docker compose stop`.

## API REST

| Método | Caminho                                                  | Resultado                        |
| ------ | -------------------------------------------------------- | -------------------------------- |
| GET    | `/api/v1/clientes?busca=maria&ativo=true&page=0&size=10` | Lista paginada                   |
| GET    | `/api/v1/clientes/{id}`                                  | Consulta um cliente              |
| POST   | `/api/v1/clientes`                                       | Cria um cliente (201 e Location) |
| PUT    | `/api/v1/clientes/{id}`                                  | Atualiza um cliente              |
| DELETE | `/api/v1/clientes/{id}`                                  | Exclui um cliente (204)          |

`busca` e `ativo` são opcionais; `page` começa em zero e `size` aceita de 1 a 100. A ordenação é por nome e ID.

```bash
curl -i http://localhost:8080/api/v1/clientes \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Maria Silva","email":"maria@example.com","telefone":"11999998888","endereco":"Rua das Flores, 100","cidade":"São Paulo","uf":"SP","cep":"01001000","ativo":true}'
```

Nome, e-mail, telefone e `ativo` são obrigatórios. Campos opcionais podem ser omitidos ou enviados como `null`. O CEP deve conter oito números e a UF deve ser uma sigla brasileira válida. O PUT substitui os dados editáveis completos. Erros retornam HTTP 400 (dados inválidos), 404 (cliente inexistente) ou 409 (e-mail duplicado).

## Testes e build

```bash
# Todos os testes: requer Docker ativo para o PostgreSQL do Testcontainers
mvn verify

# Somente testes unitários, sem Docker
mvn -Dtest=ClienteServiceTest,ClienteControllerTest test

# Gerar JAR depois de executar a suíte
mvn package
java -jar target/cadastro-clientes-1.0.0.jar
```

O GitHub Actions executa a suíte completa em cada push e pull request. O banco dos testes é descartável e separado do banco local.

## Organização

- `ClienteController`: contrato HTTP da API.
- `ClienteService`: regras de cadastro e normalização.
- `ClienteRepository`: consultas e persistência.
- `ClienteRequest`: validação de entrada.
- `ApiExceptionHandler`: respostas de erro.
- `src/main/resources/db/migration`: migrações SQL.
- `src/main/resources/static`: interface web servida pelo Spring Boot.

## Licença

GPL-3.0. Consulte [LICENSE](LICENSE).
