package controle.financeiro.dtos;

public record AuthResponseDTO (
        String token,
        String nome,
        String email
) {}
