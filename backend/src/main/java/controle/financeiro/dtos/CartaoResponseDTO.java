package controle.financeiro.dtos;

import controle.financeiro.models.CartaoCredito;
import java.math.BigDecimal;
import java.util.UUID;

public record CartaoResponseDTO(
        UUID id, String nome, String bandeira, BigDecimal limite, Integer diaFechamento, Integer diaVencimento, UUID contaPagamentoId, boolean ativo
) {
    public static CartaoResponseDTO de(CartaoCredito c) {
        return new CartaoResponseDTO(c.getId(), c.getNome(), c.getBandeira(), c.getLimite(),
                c.getDiaFechamento(), c.getDiaVencimento(),
                c.getContaPagamento() != null ? c.getContaPagamento().getId() : null,
                c.isAtivo());
    }
}
