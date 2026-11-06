package controle.financeiro.dtos;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequestDTO(@NotBlank String idToken) {}
