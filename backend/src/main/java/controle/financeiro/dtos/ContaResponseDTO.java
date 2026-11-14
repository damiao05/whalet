package controle.financeiro.dtos;

import controle.financeiro.models.Conta;
import controle.financeiro.models.TipoConta;
import java.math.BigDecimal;
import java.util.UUID;

public record ContaResponseDTO(
        UUID id, String nome, TipoConta tipo, String instituicao,
        BigDecimal saldoInicial, BigDecimal saldoAtual, boolean ativa
) {
    public static ContaResponseDTO de(Conta c, BigDecimal movimentacao) {
        return new ContaResponseDTO(c.getId(), c.getNome(), c.getTipo(), c.getInstituicao(),
                c.getSaldoInicial(), c.getSaldoInicial().add(movimentacao), c.isAtiva());
    }
}