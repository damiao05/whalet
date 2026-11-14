package controle.financeiro.dtos;

import controle.financeiro.models.TipoTransacao;
import controle.financeiro.models.Transacao;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransacaoResponseDTO(
        UUID id, String descricao, BigDecimal valor, TipoTransacao tipo,
        LocalDate data, UUID contaId, UUID cartaoId, UUID categoriaId, String metadados
) {
    public static TransacaoResponseDTO de(Transacao t) {
        return new TransacaoResponseDTO(
                t.getId(), t.getDescricao(), t.getValor(), t.getTipo(), t.getDataTransacao(),
                t.getConta() != null ? t.getConta().getId() : null,
                t.getCartao() != null ? t.getCartao().getId() : null,
                t.getCategoria() != null ? t.getCategoria().getId() : null,
                t.getMetadados());
    }
}