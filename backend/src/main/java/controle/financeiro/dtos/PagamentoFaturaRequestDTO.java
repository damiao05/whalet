package controle.financeiro.dtos;

import controle.financeiro.models.StatusTransacao;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PagamentoFaturaRequestDTO(
        @NotNull UUID contaId,
        @NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal valor,
        @NotNull LocalDate data,
        @NotBlank @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Mês deve estar no formato yyyy-MM") String mes,
        StatusTransacao status
) {}