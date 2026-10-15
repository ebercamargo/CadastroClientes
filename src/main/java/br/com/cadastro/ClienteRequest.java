package br.com.cadastro;

import jakarta.validation.constraints.*;

public record ClienteRequest(
    @NotBlank @Size(max = 120) String nome,
    @NotBlank @Email @Size(max = 180) String email,
    @NotBlank @Pattern(regexp = "[0-9+() \u002d]{8,20}", message = "Informe um telefone válido")
        String telefone,
    @Size(max = 200) String endereco,
    @Size(max = 80) String cidade,
    @Pattern(
            regexp =
                "(?i)AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO",
            message = "Informe uma UF válida")
        String uf,
    @Pattern(regexp = "[0-9]{8}", message = "Informe o CEP com 8 números") String cep,
    @NotNull Boolean ativo) {}
