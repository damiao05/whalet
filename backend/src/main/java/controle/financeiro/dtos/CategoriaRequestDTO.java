package controle.financeiro.dtos;

import controle.financeiro.models.TipoTransacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoriaRequestDTO(
        @NotBlank @Size(max = 60) String nome,
        @NotNull TipoTransacao tipo,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor deve estar no formato #RRGGBB") String cor
) {}