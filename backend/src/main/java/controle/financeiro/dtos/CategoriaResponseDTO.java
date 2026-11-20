package controle.financeiro.dtos;

import controle.financeiro.models.Categoria;
import controle.financeiro.models.TipoTransacao;
import java.util.UUID;

public record CategoriaResponseDTO(UUID id, String nome, TipoTransacao tipo, String cor, boolean ativa) {
    public static CategoriaResponseDTO de(Categoria c) {
        return new CategoriaResponseDTO(c.getId(), c.getNome(), c.getTipo(), c.getCor(), c.isAtiva());
    }
}