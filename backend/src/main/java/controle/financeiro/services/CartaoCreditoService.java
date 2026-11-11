package controle.financeiro.services;

import controle.financeiro.dtos.CartaoRequestDTO;
import controle.financeiro.dtos.CartaoResponseDTO;
import controle.financeiro.models.CartaoCredito;
import controle.financeiro.models.Conta;
import controle.financeiro.repositories.CartaoCreditoRepository;
import controle.financeiro.repositories.ContaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartaoCreditoService {

    private final CartaoCreditoRepository repository;
    private final ContaRepository contaRepository;
    private final UsuarioLogado usuarioLogado;

    @Transactional(readOnly = true)
    public List<CartaoResponseDTO> listar() {
        UUID usuarioId = usuarioLogado.obter().getId();
        return repository.findByUsuarioIdAndAtivoTrueOrderByNomeAsc(usuarioId)
                .stream().map(CartaoResponseDTO::de).toList();
    }

    @Transactional
    public CartaoResponseDTO criar(CartaoRequestDTO dto) {
        CartaoCredito cartao = new CartaoCredito();
        cartao.setUsuario(usuarioLogado.obter());
        aplicar(cartao, dto);
        return CartaoResponseDTO.de(repository.save(cartao));
    }

    @Transactional
    public CartaoResponseDTO atualizar(UUID id, CartaoRequestDTO dto) {
        CartaoCredito cartao = buscar(id);
        aplicar(cartao, dto);
        return CartaoResponseDTO.de(cartao);
    }

    @Transactional
    public void arquivar(UUID id) {
        buscar(id).setAtivo(false);
    }

    private CartaoCredito buscar(UUID id) {
        return repository.findByIdAndUsuarioId(id, usuarioLogado.obter().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cartão não encontrado"));
    }

    private void aplicar(CartaoCredito cartao, CartaoRequestDTO dto) {
        cartao.setNome(dto.nome().trim());
        cartao.setBandeira(dto.bandeira());
        cartao.setLimite(dto.limite());
        cartao.setDiaFechamento(dto.diaFechamento());
        cartao.setDiaVencimento(dto.diaVencimento());

        Conta conta = null;
        if (dto.contaPagamentoId() != null) {
            conta = contaRepository.findByIdAndUsuarioId(dto.contaPagamentoId(), usuarioLogado.obter().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conta de pagamento inválida"));
        }
        cartao.setContaPagamento(conta);
    }

}
