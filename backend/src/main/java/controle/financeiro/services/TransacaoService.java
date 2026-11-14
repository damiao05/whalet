package controle.financeiro.services;

import controle.financeiro.dtos.TransacaoRequestDTO;
import controle.financeiro.dtos.TransacaoResponseDTO;
import controle.financeiro.models.CartaoCredito;
import controle.financeiro.models.Conta;
import controle.financeiro.models.TipoTransacao;
import controle.financeiro.models.Transacao;
import controle.financeiro.repositories.CartaoCreditoRepository;
import controle.financeiro.repositories.ContaRepository;
import controle.financeiro.repositories.TransacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final TransacaoRepository repository;
    private final ContaRepository contaRepository;
    private final CartaoCreditoRepository cartaoRepository;
    private final UsuarioLogado usuarioLogado;

    @Transactional(readOnly = true)
    public List<TransacaoResponseDTO> listar(YearMonth mes) {
        UUID usuarioId = usuarioLogado.obter().getId();
        return repository.listarPorPeriodo(usuarioId, mes.atDay(1), mes.atEndOfMonth())
                .stream().map(TransacaoResponseDTO::de).toList();
    }

    @Transactional
    public TransacaoResponseDTO criar(TransacaoRequestDTO dto) {
        Transacao transacao = new Transacao();
        transacao.setUsuario(usuarioLogado.obter());
        aplicar(transacao, dto);
        return TransacaoResponseDTO.de(repository.save(transacao));
    }

    @Transactional
    public TransacaoResponseDTO atualizar(UUID id, TransacaoRequestDTO dto) {
        Transacao transacao = buscar(id);
        aplicar(transacao, dto);
        return TransacaoResponseDTO.de(transacao);
    }

    @Transactional
    public void excluir(UUID id) {
        repository.delete(buscar(id));
    }

    private Transacao buscar(UUID id) {
        return repository.findByIdAndUsuarioId(id, usuarioLogado.obter().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transação não encontrada"));
    }

    private void aplicar(Transacao t, TransacaoRequestDTO dto) {
        // Exatamente uma origem: conta OU cartão
        if ((dto.contaId() == null) == (dto.cartaoId() == null)) {
            throw erro("Informe a conta ou o cartão (apenas um dos dois)");
        }

        UUID usuarioId = t.getUsuario().getId();
        Conta conta = null;
        CartaoCredito cartao = null;

        if (dto.contaId() != null) {
            conta = contaRepository.findByIdAndUsuarioId(dto.contaId(), usuarioId)
                    .filter(Conta::isAtiva)
                    .orElseThrow(() -> erro("Conta inválida"));
        } else {
            cartao = cartaoRepository.findByIdAndUsuarioId(dto.cartaoId(), usuarioId)
                    .filter(CartaoCredito::isAtivo)
                    .orElseThrow(() -> erro("Cartão inválido"));
            if (dto.tipo() != TipoTransacao.DESPESA) {
                throw erro("No cartão só é possível lançar despesas por enquanto");
            }
        }

        t.setConta(conta);
        t.setCartao(cartao);
        t.setDescricao(dto.descricao().trim());
        t.setValor(dto.valor());
        t.setTipo(dto.tipo());
        t.setDataTransacao(dto.data());
        t.setMetadados(dto.metadados());
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}