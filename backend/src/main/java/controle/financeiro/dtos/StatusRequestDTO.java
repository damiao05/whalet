package controle.financeiro.dtos;

import controle.financeiro.models.StatusTransacao;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record StatusRequestDTO(@NotNull StatusTransacao status, LocalDate data) {}