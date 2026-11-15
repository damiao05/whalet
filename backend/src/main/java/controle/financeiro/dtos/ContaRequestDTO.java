package controle.financeiro.dtos;

import controle.financeiro.models.TipoConta;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ContaRequestDTO (
        @NotBlank @Size(max = 100) String nome,
        @NotNull TipoConta tipo,
        @Size(max = 100) String instituicao,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor deve estar no formato #RRGGBB") String cor,
        @Pattern(regexp = "^[a-z0-9_-]{1,30}$", message = "Ícone inválido") String icon,
        @NotNull BigDecimal saldoInicial
) {}
