package controle.financeiro.repositories;

import controle.financeiro.models.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransacaoRepository extends JpaRepository<Transacao, UUID> {

    Optional<Transacao> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    @Query("""
            select t from Transacao t
            where t.usuario.id = :usuarioId
              and t.dataTransacao between :inicio and :fim
            order by t.dataTransacao desc, t.dataCriacao desc
            """)
    List<Transacao> listarPorPeriodo(@Param("usuarioId") UUID usuarioId,
                                     @Param("inicio") LocalDate inicio,
                                     @Param("fim") LocalDate fim);

    /** Receitas somam, despesas subtraem. Retorna null se a conta não tem transações. */
    @Query("""
            select sum(case when t.tipo = controle.financeiro.models.TipoTransacao.RECEITA
                            then t.valor else -t.valor end)
            from Transacao t
            where t.conta.id = :contaId
            """)
    BigDecimal somarMovimentacaoDaConta(@Param("contaId") UUID contaId);
}