package controle.financeiro.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransacaoRequestDTO (
    @NotBlank(message = "A descrição é obrigatória") String descricao,
    @NotNull(message = "O valor é obrigatório") @Positive BigDecimal valor,
    @NotBlank(message = "O tipo é obrigatório") String tipo,
    String metadados
) {}
