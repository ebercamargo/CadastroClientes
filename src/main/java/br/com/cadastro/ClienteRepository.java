package br.com.cadastro;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
  boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

  @Query(
      "select c from Cliente c where (:ativo is null or c.ativo=:ativo) and (lower(c.nome) like"
          + " lower(concat('%',:busca,'%')) or lower(c.email) like lower(concat('%',:busca,'%')))")
  Page<Cliente> pesquisar(
      @Param("busca") String busca, @Param("ativo") Boolean ativo, Pageable pageable);
}
