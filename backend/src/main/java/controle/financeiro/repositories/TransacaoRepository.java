package controle.financeiro.repositories;

import controle.financeiro.models.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransacaoRepository extends JpaRepository<Transacao, UUID> {

    Optional<Transacao> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    List<Transacao> findByGrupoParcelasIdAndUsuarioId(UUID grupoParcelasId, UUID usuarioId);

    @Query("""
            select t from Transacao t
            where t.usuario.id = :usuarioId
              and t.dataTransacao between :inicio and :fim
            order by t.dataTransacao desc, t.dataCriacao desc
            """)
    List<Transacao> listarPorPeriodo(@Param("usuarioId") UUID usuarioId,
                                     @Param("inicio") LocalDate inicio,
                                     @Param("fim") LocalDate fim);

    /**
     * Movimentação EFETIVADA da conta:
     *  - receita soma;
     *  - despesa, pagamento de fatura e transferência de SAÍDA subtraem;
     *  - transferência de ENTRADA (a conta é o destino) soma.
     * Retorna null se a conta não tem movimentações.
     */
    @Query("""
            select sum(case
                           when t.tipo = controle.financeiro.models.TipoTransacao.RECEITA then t.valor
                           when t.conta.id = :contaId then -t.valor
                           else t.valor
                       end)
            from Transacao t
            where t.status = controle.financeiro.models.StatusTransacao.EFETIVADA
              and (t.conta.id = :contaId or t.contaDestino.id = :contaId)
            """)
    BigDecimal somarMovimentacaoDaConta(@Param("contaId") UUID contaId);

    @Query("""
            select t from Transacao t
            where t.cartao.id = :cartaoId
              and t.tipo = controle.financeiro.models.TipoTransacao.DESPESA
              and t.dataTransacao between :inicio and :fim
            order by t.dataTransacao, t.dataCriacao
            """)
    List<Transacao> comprasDoCartaoNoPeriodo(@Param("cartaoId") UUID cartaoId,
                                             @Param("inicio") LocalDate inicio,
                                             @Param("fim") LocalDate fim);

    @Query("""
            select t from Transacao t
            where t.cartao.id = :cartaoId
              and t.tipo = controle.financeiro.models.TipoTransacao.PAGAMENTO_FATURA
              and t.faturaMes = :mes
            order by t.dataTransacao
            """)
    List<Transacao> pagamentosDaFatura(@Param("cartaoId") UUID cartaoId, @Param("mes") LocalDate mes);

    /** Dívida do cartão = compras efetivadas − pagamentos efetivados (null se não há nada). */
    @Query("""
            select sum(case when t.tipo = controle.financeiro.models.TipoTransacao.PAGAMENTO_FATURA
                            then -t.valor else t.valor end)
            from Transacao t
            where t.cartao.id = :cartaoId
              and t.status = controle.financeiro.models.StatusTransacao.EFETIVADA
            """)
    BigDecimal somarDividaDoCartao(@Param("cartaoId") UUID cartaoId);

    // ---- usados pela recorrência (Parte 2) ----

    @Query("""
            select t from Transacao t
            where t.recorrenciaId = :recorrenciaId
              and t.status = controle.financeiro.models.StatusTransacao.PENDENTE
              and t.dataTransacao > :hoje
            """)
    List<Transacao> futurasPendentesDaRecorrencia(@Param("recorrenciaId") UUID recorrenciaId,
                                                  @Param("hoje") LocalDate hoje);

    /** Cobranças de cartão (ex.: assinaturas) viram efetivadas quando a data chega. */
    @Modifying
    @Query("""
            update Transacao t set t.status = controle.financeiro.models.StatusTransacao.EFETIVADA
            where t.status = controle.financeiro.models.StatusTransacao.PENDENTE
              and t.cartao is not null
              and t.tipo = controle.financeiro.models.TipoTransacao.DESPESA
              and t.dataTransacao <= :hoje
            """)
    int efetivarComprasDeCartaoVencidas(@Param("hoje") LocalDate hoje);

    @Query("""
        select t from Transacao t
        where t.usuario.id = :usuarioId
          and t.status = controle.financeiro.models.StatusTransacao.PENDENTE
          and t.cartao is null
          and t.dataTransacao <= :ate
        order by t.dataTransacao, t.dataCriacao
        """)
    List<Transacao> pendentesAte(@Param("usuarioId") UUID usuarioId, @Param("ate") LocalDate ate);
}