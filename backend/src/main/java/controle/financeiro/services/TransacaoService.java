package controle.financeiro.services;

import controle.financeiro.dtos.StatusRequestDTO;
import controle.financeiro.dtos.TransacaoRequestDTO;
import controle.financeiro.dtos.TransacaoResponseDTO;
import controle.financeiro.services.VinculosResolver.Vinculos;
import controle.financeiro.models.*;
import controle.financeiro.repositories.CartaoCreditoRepository;
import controle.financeiro.repositories.CategoriaRepository;
import controle.financeiro.repositories.ContaRepository;
import controle.financeiro.repositories.TransacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    // O servidor em produção costuma estar em UTC; "hoje" precisa ser o de Brasília.
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private final TransacaoRepository repository;
    private final VinculosResolver vinculosResolver;
    private final ContaRepository contaRepository;
    private final UsuarioLogado usuarioLogado;

    private Vinculos resolver(TransacaoRequestDTO dto, UUID usuarioId) {
        return vinculosResolver.resolver(dto.tipo(), dto.contaId(), dto.contaDestinoId(),
                dto.cartaoId(), dto.categoriaId(), usuarioId);
    }

    @Transactional(readOnly = true)
    public List<TransacaoResponseDTO> listar(YearMonth mes) {
        UUID usuarioId = usuarioLogado.obter().getId();
        return repository.listarPorPeriodo(usuarioId, mes.atDay(1), mes.atEndOfMonth())
                .stream().map(TransacaoResponseDTO::de).toList();
    }

    /** Cria 1 lançamento ou, com "parcelas", N lançamentos (um por mês). */
    @Transactional
    public List<TransacaoResponseDTO> criar(TransacaoRequestDTO dto) {
        Usuario usuario = usuarioLogado.obter();
        Vinculos v = resolver(dto, usuario.getId());

        int n = dto.parcelas() != null ? dto.parcelas() : 1;
        long[] valores = dividir(dto.valor(), n);
        UUID grupo = n > 1 ? UUID.randomUUID() : null;

        List<Transacao> novas = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Transacao t = new Transacao();
            t.setUsuario(usuario);
            preencher(t, dto, v);
            t.setValor(BigDecimal.valueOf(valores[i], 2));
            // Sempre a partir da data original, para o dia 31 não "derreter" para 28 nos meses seguintes
            t.setDataTransacao(dto.data().plusMonths(i));
            t.setStatus(statusPara(dto, v, t.getDataTransacao(), i == 0));
            if (grupo != null) {
                t.setGrupoParcelasId(grupo);
                t.setParcelaNumero(i + 1);
                t.setParcelaTotal(n);
            }
            novas.add(t);
        }
        return repository.saveAll(novas).stream().map(TransacaoResponseDTO::de).toList();
    }

    @Transactional
    public TransacaoResponseDTO atualizar(UUID id, TransacaoRequestDTO dto) {
        if (dto.parcelas() != null) {
            throw erro("Não é possível parcelar ao editar um lançamento");
        }
        Transacao t = buscar(id);
        Vinculos v = resolver(dto, t.getUsuario().getId());
        preencher(t, dto, v);
        t.setValor(dto.valor());
        t.setDataTransacao(dto.data());
        if (v.cartao() != null) {
            if (t.getRecorrenciaId() == null) {
                t.setStatus(StatusTransacao.EFETIVADA);
            }
        } else if (dto.status() != null) {
            t.setStatus(dto.status());
        }
        return TransacaoResponseDTO.de(t);
    }

    /** Marca como pago/recebido (ou volta para pendente), opcionalmente ajustando a data. */
    @Transactional
    public TransacaoResponseDTO alterarStatus(UUID id, StatusRequestDTO dto) {
        Transacao t = buscar(id);
        if (t.getCartao() != null && t.getTipo() == TipoTransacao.DESPESA) {
            throw erro("Compras no cartão não têm status; o que se paga é a fatura");
        }
        t.setStatus(dto.status());
        if (dto.data() != null) {
            t.setDataTransacao(dto.data());
        }
        return TransacaoResponseDTO.de(t);
    }

    @Transactional
    public void excluir(UUID id, boolean todasParcelas) {
        Transacao t = buscar(id);
        if (todasParcelas && t.getGrupoParcelasId() != null) {
            repository.deleteAll(
                    repository.findByGrupoParcelasIdAndUsuarioId(t.getGrupoParcelasId(), t.getUsuario().getId()));
        } else {
            repository.delete(t);
        }
    }

    @Transactional(readOnly = true)
    public List<TransacaoResponseDTO> pendentes(LocalDate ate) {
        UUID usuarioId = usuarioLogado.obter().getId();
        return repository.pendentesAte(usuarioId, ate).stream().map(TransacaoResponseDTO::de).toList();
    }

    // ----------------------------------------------------------------------------------

    private Transacao buscar(UUID id) {
        return repository.findByIdAndUsuarioId(id, usuarioLogado.obter().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transação não encontrada"));
    }

    private Conta contaAtiva(UUID id, UUID usuarioId) {
        return contaRepository.findByIdAndUsuarioId(id, usuarioId)
                .filter(Conta::isAtiva)
                .orElseThrow(() -> erro("Conta inválida"));
    }

    private void preencher(Transacao t, TransacaoRequestDTO dto, Vinculos v) {
        t.setDescricao(dto.descricao().trim());
        t.setTipo(dto.tipo());
        t.setConta(v.conta());
        t.setContaDestino(v.contaDestino());
        t.setCartao(v.cartao());
        t.setCategoria(v.categoria());
        t.setMetadados(dto.metadados());
    }

    /** Cartão: sempre efetivada. Conta: o que o usuário informou (só na 1ª parcela) ou, se omitido, pela data. */
    private StatusTransacao statusPara(TransacaoRequestDTO dto, Vinculos v, LocalDate data, boolean primeira) {
        if (v.cartao() != null) return StatusTransacao.EFETIVADA;
        if (dto.status() != null && primeira) return dto.status();
        return data.isAfter(LocalDate.now(FUSO)) ? StatusTransacao.PENDENTE : StatusTransacao.EFETIVADA;
    }

    /** Divide o total em N partes, em centavos; as primeiras levam os centavos que sobram. */
    private long[] dividir(BigDecimal total, int n) {
        long centavos = total.movePointRight(2).longValueExact();
        if (centavos < n) {
            throw erro("Valor pequeno demais para esse número de parcelas");
        }
        long base = centavos / n;
        long resto = centavos % n;
        long[] partes = new long[n];
        for (int i = 0; i < n; i++) {
            partes[i] = base + (i < resto ? 1 : 0);
        }
        return partes;
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}