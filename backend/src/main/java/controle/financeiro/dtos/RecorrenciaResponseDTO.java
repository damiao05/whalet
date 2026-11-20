package controle.financeiro.dtos;

import controle.financeiro.models.Frequencia;
import controle.financeiro.models.Recorrencia;
import controle.financeiro.models.TipoTransacao;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecorrenciaResponseDTO(
        UUID id, String descricao, BigDecimal valor, TipoTransacao tipo,
        UUID contaId, UUID contaDestinoId, UUID cartaoId, UUID categoriaId,
        Frequencia frequencia, Integer intervalo, LocalDate dataInicio, LocalDate dataFim,
        LocalDate geradoAte, boolean ativa
) {
    public static RecorrenciaResponseDTO de(Recorrencia r) {
        return new RecorrenciaResponseDTO(
                r.getId(), r.getDescricao(), r.getValor(), r.getTipo(),
                r.getConta() != null ? r.getConta().getId() : null,
                r.getContaDestino() != null ? r.getContaDestino().getId() : null,
                r.getCartao() != null ? r.getCartao().getId() : null,
                r.getCategoria() != null ? r.getCategoria().getId() : null,
                r.getFrequencia(), r.getIntervalo(), r.getDataInicio(), r.getDataFim(),
                r.getGeradoAte(), r.isAtiva());
    }
}