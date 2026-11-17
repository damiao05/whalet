package controle.financeiro.repositories;

import controle.financeiro.models.Recorrencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecorrenciaRepository extends JpaRepository<Recorrencia, UUID> {

    List<Recorrencia> findByUsuarioIdAndAtivaTrueOrderByDescricaoAsc(UUID usuarioId);

    Optional<Recorrencia> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    List<Recorrencia> findByAtivaTrue();
}