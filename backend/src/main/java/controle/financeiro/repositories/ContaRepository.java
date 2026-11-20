package controle.financeiro.repositories;

import controle.financeiro.models.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContaRepository extends JpaRepository<Conta, UUID> {

    List<Conta> findByUsuarioIdAndAtivaTrueOrderByNomeAsc(UUID usuarioId);

    Optional<Conta> findByIdAndUsuarioId(UUID id, UUID usuarioId);

}
