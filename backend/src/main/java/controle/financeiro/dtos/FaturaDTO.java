package controle.financeiro.dtos;

import controle.financeiro.models.StatusFatura;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record FaturaDTO(
        UUID cartaoId,
        String mes,                    // mês de vencimento, "2026-12"
        LocalDate dataInicio,
        LocalDate dataFechamento,
        LocalDate dataVencimento,
        BigDecimal total,
        BigDecimal pago,
        BigDecimal emAberto,
        StatusFatura status,
        List<TransacaoResponseDTO> compras,
        List<TransacaoResponseDTO> pagamentos
) {}