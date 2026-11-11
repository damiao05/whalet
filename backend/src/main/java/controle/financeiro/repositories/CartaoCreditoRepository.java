package controle.financeiro.repositories;

import controle.financeiro.models.CartaoCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartaoCreditoRepository extends JpaRepository<CartaoCredito, UUID> {

    List<CartaoCredito> findByUsuarioIdAndAtivoTrueOrderByNomeAsc(UUID usuarioId);

    Optional<CartaoCredito> findByIdAndUsuarioId(UUID id, UUID usuarioId);

}
