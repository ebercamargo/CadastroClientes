package br.com.cadastro;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "clientes")
public class Cliente {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, length = 120)
  public String nome;

  @Column(nullable = false, length = 180)
  public String email;

  @Column(nullable = false, length = 20)
  public String telefone;

  @Column(length = 200)
  public String endereco;

  @Column(length = 80)
  public String cidade;

  @Column(length = 2)
  public String uf;

  @Column(length = 8)
  public String cep;

  @Column(nullable = false)
  public boolean ativo = true;

  @Column(nullable = false, updatable = false)
  public Instant criadoEm;

  @Column(nullable = false)
  public Instant atualizadoEm;

  @PrePersist
  void criar() {
    criadoEm = Instant.now();
    atualizadoEm = criadoEm;
  }

  @PreUpdate
  void atualizar() {
    atualizadoEm = Instant.now();
  }
}
