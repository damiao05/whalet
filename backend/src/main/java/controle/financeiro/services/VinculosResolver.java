package controle.financeiro.services;

import controle.financeiro.models.*;
import controle.financeiro.repositories.CartaoCreditoRepository;
import controle.financeiro.repositories.CategoriaRepository;
import controle.financeiro.repositories.ContaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VinculosResolver {

    /** Entidades já validadas (todas pertencem ao usuário). */
    public record Vinculos(Conta conta, Conta contaDestino, CartaoCredito cartao, Categoria categoria) {}

    private final ContaRepository contaRepository;
    private final CartaoCreditoRepository cartaoRepository;
    private final CategoriaRepository categoriaRepository;

    public Vinculos resolver(TipoTransacao tipo, UUID contaId, UUID contaDestinoId,
                             UUID cartaoId, UUID categoriaId, UUID usuarioId) {
        return switch (tipo) {
            case PAGAMENTO_FATURA -> throw erro("O pagamento de fatura tem endpoint próprio");

            case TRANSFERENCIA -> {
                if (contaId == null || contaDestinoId == null) {
                    throw erro("Informe a conta de origem e a de destino");
                }
                if (contaId.equals(contaDestinoId)) {
                    throw erro("A origem e o destino precisam ser contas diferentes");
                }
                if (cartaoId != null || categoriaId != null) {
                    throw erro("Transferência não usa cartão nem categoria");
                }
                yield new Vinculos(contaAtiva(contaId, usuarioId), contaAtiva(contaDestinoId, usuarioId), null, null);
            }

            case RECEITA, DESPESA -> {
                if (contaDestinoId != null) {
                    throw erro("Conta de destino só vale para transferências");
                }
                if ((contaId == null) == (cartaoId == null)) {
                    throw erro("Informe a conta ou o cartão (apenas um dos dois)");
                }

                Conta conta = null;
                CartaoCredito cartao = null;
                if (contaId != null) {
                    conta = contaAtiva(contaId, usuarioId);
                } else {
                    if (tipo != TipoTransacao.DESPESA) {
                        throw erro("No cartão só é possível lançar despesas");
                    }
                    cartao = cartaoRepository.findByIdAndUsuarioId(cartaoId, usuarioId)
                            .filter(CartaoCredito::isAtivo)
                            .orElseThrow(() -> erro("Cartão inválido"));
                }

                Categoria categoria = null;
                if (categoriaId != null) {
                    categoria = categoriaRepository.findByIdAndUsuarioId(categoriaId, usuarioId)
                            .filter(Categoria::isAtiva)
                            .orElseThrow(() -> erro("Categoria inválida"));
                    if (categoria.getTipo() != tipo) {
                        throw erro("A categoria não é do mesmo tipo da transação");
                    }
                }
                yield new Vinculos(conta, null, cartao, categoria);
            }
        };
    }

    private Conta contaAtiva(UUID id, UUID usuarioId) {
        return contaRepository.findByIdAndUsuarioId(id, usuarioId)
                .filter(Conta::isAtiva)
                .orElseThrow(() -> erro("Conta inválida"));
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}