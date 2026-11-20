package controle.financeiro.dtos;

import controle.financeiro.models.StatusTransacao;
import controle.financeiro.models.TipoTransacao;
import controle.financeiro.models.Transacao;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransacaoResponseDTO(
        UUID id, String descricao, BigDecimal valor, TipoTransacao tipo, StatusTransacao status,
        LocalDate data, UUID contaId, UUID contaDestinoId, UUID cartaoId, UUID categoriaId,
        Integer parcelaNumero, Integer parcelaTotal, UUID grupoParcelasId, UUID recorrenciaId,
        LocalDate faturaMes, String metadados
) {
    public static TransacaoResponseDTO de(Transacao t) {
        return new TransacaoResponseDTO(
                t.getId(), t.getDescricao(), t.getValor(), t.getTipo(), t.getStatus(), t.getDataTransacao(),
                t.getConta() != null ? t.getConta().getId() : null,
                t.getContaDestino() != null ? t.getContaDestino().getId() : null,
                t.getCartao() != null ? t.getCartao().getId() : null,
                t.getCategoria() != null ? t.getCategoria().getId() : null,
                t.getParcelaNumero(), t.getParcelaTotal(), t.getGrupoParcelasId(), t.getRecorrenciaId(),
                t.getFaturaMes(), t.getMetadados());
    }
}