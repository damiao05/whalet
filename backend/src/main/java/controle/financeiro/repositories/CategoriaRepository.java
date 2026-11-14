package controle.financeiro.repositories;

import controle.financeiro.models.Categoria;
import controle.financeiro.models.TipoTransacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {

    List<Categoria> findByUsuarioIdOrderByTipoAscNomeAsc(UUID usuarioId);

    Optional<Categoria> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    boolean existsByUsuarioIdAndTipoAndNomeIgnoreCaseAndAtivaTrue(UUID usuarioId, TipoTransacao tipo, String nome);
}