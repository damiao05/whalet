package controle.financeiro.dtos;

import controle.financeiro.models.Frequencia;
import controle.financeiro.models.TipoTransacao;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecorrenciaRequestDTO(
        @NotBlank @Size(max = 255) String descricao,
        @NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal valor,
        @NotNull TipoTransacao tipo,
        UUID contaId,
        UUID contaDestinoId,
        UUID cartaoId,
        UUID categoriaId,
        @NotNull Frequencia frequencia,
        @NotNull @Min(1) @Max(12) Integer intervalo,
        @NotNull LocalDate dataInicio,
        LocalDate dataFim
) {}