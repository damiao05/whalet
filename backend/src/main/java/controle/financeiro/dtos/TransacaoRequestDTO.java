package controle.financeiro.dtos;

import controle.financeiro.models.TipoTransacao;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransacaoRequestDTO(
        @NotBlank @Size(max = 255) String descricao,
        @NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal valor,
        @NotNull TipoTransacao tipo,
        @NotNull LocalDate data,
        UUID contaId,
        UUID cartaoId,
        String metadados
) {}