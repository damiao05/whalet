package controle.financeiro.dtos;

import controle.financeiro.models.TipoConta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ContaRequestDTO (
        @NotBlank @Size(max = 100) String nome,
        @NotNull TipoConta tipo,
        @Size(max = 100) String instituicao,
        @NotNull BigDecimal saldoInicial
) {}
