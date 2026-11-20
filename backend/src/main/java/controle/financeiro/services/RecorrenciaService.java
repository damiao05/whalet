package controle.financeiro.services;

import controle.financeiro.dtos.RecorrenciaRequestDTO;
import controle.financeiro.dtos.RecorrenciaResponseDTO;
import controle.financeiro.models.*;
import controle.financeiro.repositories.RecorrenciaRepository;
import controle.financeiro.repositories.TransacaoRepository;
import controle.financeiro.services.VinculosResolver.Vinculos;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecorrenciaService {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final int MESES_A_FRENTE = 12;
    private static final int LIMITE_DE_ITERACOES = 2000;

    private final RecorrenciaRepository repository;
    private final TransacaoRepository transacaoRepository;
    private final VinculosResolver vinculosResolver;
    private final UsuarioLogado usuarioLogado;

    @Transactional(readOnly = true)
    public List<RecorrenciaResponseDTO> listar() {
        UUID usuarioId = usuarioLogado.obter().getId();
        return repository.findByUsuarioIdAndAtivaTrueOrderByDescricaoAsc(usuarioId)
                .stream().map(RecorrenciaResponseDTO::de).toList();
    }

    @Transactional
    public RecorrenciaResponseDTO criar(RecorrenciaRequestDTO dto) {
        Usuario usuario = usuarioLogado.obter();
        validarDatas(dto);
        Vinculos v = vincular(dto, usuario.getId());

        Recorrencia r = new Recorrencia();
        r.setUsuario(usuario);
        r.setFrequencia(dto.frequencia());
        r.setIntervalo(dto.intervalo());
        r.setDataInicio(dto.dataInicio());
        aplicar(r, dto, v);
        repository.save(r);

        gerar(r);
        return RecorrenciaResponseDTO.de(r);
    }

    /** Altera o modelo e as ocorrências futuras ainda pendentes (as já pagas e as editadas à mão no passado ficam). */
    @Transactional
    public RecorrenciaResponseDTO atualizar(UUID id, RecorrenciaRequestDTO dto) {
        Recorrencia r = buscar(id);
        if (dto.frequencia() != r.getFrequencia()
                || !dto.intervalo().equals(r.getIntervalo())
                || !dto.dataInicio().equals(r.getDataInicio())) {
            throw erro("Para mudar a frequência ou a data de início, encerre esta recorrência e crie outra");
        }
        validarDatas(dto);
        aplicar(r, dto, vincular(dto, r.getUsuario().getId()));

        for (Transacao t : transacaoRepository.futurasPendentesDaRecorrencia(r.getId(), hoje())) {
            if (r.getDataFim() != null && t.getDataTransacao().isAfter(r.getDataFim())) {
                transacaoRepository.delete(t);
            } else {
                copiar(r, t);
            }
        }
        if (r.getDataFim() != null && r.getGeradoAte() != null && r.getGeradoAte().isAfter(r.getDataFim())) {
            r.setGeradoAte(r.getDataFim());
        }
        gerar(r); // se a data final foi estendida ou removida, gera o que faltar
        return RecorrenciaResponseDTO.de(r);
    }

    /** Encerra: nada novo é gerado e as futuras pendentes são apagadas. O histórico fica. */
    @Transactional
    public void encerrar(UUID id) {
        Recorrencia r = buscar(id);
        r.setAtiva(false);
        transacaoRepository.deleteAll(transacaoRepository.futurasPendentesDaRecorrencia(r.getId(), hoje()));
    }

    /** Rotina diária: estende o horizonte das recorrências e efetiva cobranças de cartão que já venceram. */
    @Transactional
    public void manutencao() {
        repository.findByAtivaTrue().forEach(this::gerar);
        transacaoRepository.efetivarComprasDeCartaoVencidas(hoje());
    }

    // ------------------------------------------------------------------------------

    private void gerar(Recorrencia r) {
        LocalDate hoje = hoje();
        LocalDate horizonte = hoje.plusMonths(MESES_A_FRENTE);
        List<Transacao> novas = new ArrayList<>();

        for (int i = 0; i < LIMITE_DE_ITERACOES; i++) {
            LocalDate data = ocorrencia(r, i);
            if (data.isAfter(horizonte)) break;
            if (r.getDataFim() != null && data.isAfter(r.getDataFim())) break;
            if (r.getGeradoAte() != null && !data.isAfter(r.getGeradoAte())) continue; // já gerada

            Transacao t = new Transacao();
            t.setUsuario(r.getUsuario());
            t.setRecorrenciaId(r.getId());
            copiar(r, t);
            t.setDataTransacao(data);
            t.setStatus(r.getCartao() != null && !data.isAfter(hoje)
                    ? StatusTransacao.EFETIVADA : StatusTransacao.PENDENTE);
            novas.add(t);
            r.setGeradoAte(data);
        }
        transacaoRepository.saveAll(novas);
    }

    /** Data da i-ésima ocorrência, sempre calculada a partir da data inicial (o dia 31 não "derrete"). */
    private LocalDate ocorrencia(Recorrencia r, int i) {
        long passo = (long) i * r.getIntervalo();
        return switch (r.getFrequencia()) {
            case SEMANAL -> r.getDataInicio().plusWeeks(passo);
            case MENSAL -> r.getDataInicio().plusMonths(passo);
            case ANUAL -> r.getDataInicio().plusYears(passo);
        };
    }

    private void copiar(Recorrencia r, Transacao t) {
        t.setDescricao(r.getDescricao());
        t.setValor(r.getValor());
        t.setTipo(r.getTipo());
        t.setConta(r.getConta());
        t.setContaDestino(r.getContaDestino());
        t.setCartao(r.getCartao());
        t.setCategoria(r.getCategoria());
    }

    private void aplicar(Recorrencia r, RecorrenciaRequestDTO dto, Vinculos v) {
        r.setDescricao(dto.descricao().trim());
        r.setValor(dto.valor());
        r.setTipo(dto.tipo());
        r.setConta(v.conta());
        r.setContaDestino(v.contaDestino());
        r.setCartao(v.cartao());
        r.setCategoria(v.categoria());
        r.setDataFim(dto.dataFim());
    }

    private Vinculos vincular(RecorrenciaRequestDTO dto, UUID usuarioId) {
        return vinculosResolver.resolver(dto.tipo(), dto.contaId(), dto.contaDestinoId(),
                dto.cartaoId(), dto.categoriaId(), usuarioId);
    }

    private void validarDatas(RecorrenciaRequestDTO dto) {
        if (dto.dataFim() != null && dto.dataFim().isBefore(dto.dataInicio())) {
            throw erro("A data final não pode ser anterior à data de início");
        }
    }

    private Recorrencia buscar(UUID id) {
        return repository.findByIdAndUsuarioId(id, usuarioLogado.obter().getId())
                .filter(Recorrencia::isAtiva)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recorrência não encontrada"));
    }

    private LocalDate hoje() {
        return LocalDate.now(FUSO);
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}