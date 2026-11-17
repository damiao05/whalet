package controle.financeiro.dtos;

import controle.financeiro.models.CartaoCredito;
import java.math.BigDecimal;
import java.util.UUID;

public record CartaoResponseDTO(
        UUID id, String nome, String bandeira,
        BigDecimal limite, BigDecimal limiteUsado, BigDecimal limiteDisponivel,
        Integer diaFechamento, Integer diaVencimento, UUID contaPagamentoId,
        String cor, String icon, boolean ativo
) {
    public static CartaoResponseDTO de(CartaoCredito c, BigDecimal usado) {
        return new CartaoResponseDTO(
                c.getId(), c.getNome(), c.getBandeira(),
                c.getLimite(), usado, c.getLimite().subtract(usado),
                c.getDiaFechamento(), c.getDiaVencimento(),
                c.getContaPagamento() != null ? c.getContaPagamento().getId() : null,
                c.getCor(), c.getIcon(), c.isAtivo());
    }
}