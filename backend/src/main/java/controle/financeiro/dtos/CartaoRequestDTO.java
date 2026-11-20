package controle.financeiro.dtos;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record CartaoRequestDTO(
        @NotBlank @Size(max = 100) String nome,
        @Size(max = 30) String bandeira,
        @NotNull @PositiveOrZero BigDecimal limite,
        @NotNull @Min(1) @Max(31) Integer diaFechamento,
        @NotNull @Min(1) @Max(31) Integer diaVencimento,
        UUID contaPagamentoId,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor deve estar no formato #RRGGBB") String cor,
        @Pattern(regexp = "^[a-z0-9_-]{1,30}$", message = "Ícone inválido") String icon
) {}
