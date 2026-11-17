package controle.financeiro.services;

import controle.financeiro.dtos.FaturaDTO;
import controle.financeiro.dtos.PagamentoFaturaRequestDTO;
import controle.financeiro.dtos.TransacaoResponseDTO;
import controle.financeiro.models.*;
import controle.financeiro.repositories.CartaoCreditoRepository;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FaturaService {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter MES_ANO = DateTimeFormatter.ofPattern("MM/yyyy");

    private final CartaoCreditoRepository cartaoRepository;
    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;
    private final UsuarioLogado usuarioLogado;

    /** Fatura do mês de vencimento informado; sem o parâmetro, a fatura em que uma compra de hoje cairia. */
    @Transactional(readOnly = true)
    public FaturaDTO obter(UUID cartaoId, YearMonth mesVencimento) {
        CartaoCredito cartao = cartaoDoUsuario(cartaoId);
        int fech = cartao.getDiaFechamento();
        int venc = cartao.getDiaVencimento();
        LocalDate hoje = LocalDate.now(FUSO);

        YearMonth mes = mesVencimento != null ? mesVencimento : CicloFatura.faturaDaCompra(hoje, fech, venc);
        YearMonth mesFech = CicloFatura.mesFechamentoDaFatura(mes, fech, venc);
        LocalDate fechamento = CicloFatura.dataFechamento(mesFech, fech);
        LocalDate inicio = CicloFatura.dataFechamento(mesFech.minusMonths(1), fech).plusDays(1);
        LocalDate vencimento = CicloFatura.dataVencimento(mes, venc);

        List<Transacao> compras = transacaoRepository.comprasDoCartaoNoPeriodo(cartaoId, inicio, fechamento);
        List<Transacao> pagamentos = transacaoRepository.pagamentosDaFatura(cartaoId, mes.atDay(1));

        BigDecimal total = soma(compras.stream());
        BigDecimal pago = soma(pagamentos.stream().filter(p -> p.getStatus() == StatusTransacao.EFETIVADA));
        BigDecimal emAberto = total.subtract(pago);

        StatusFatura status;
        if (total.signum() > 0 && emAberto.signum() <= 0) status = StatusFatura.PAGA;
        else if (total.signum() > 0 && hoje.isAfter(vencimento)) status = StatusFatura.VENCIDA;
        else if (hoje.isAfter(fechamento)) status = StatusFatura.FECHADA;
        else status = StatusFatura.ABERTA;

        return new FaturaDTO(cartaoId, mes.toString(), inicio, fechamento, vencimento,
                total, pago, emAberto, status,
                compras.stream().map(TransacaoResponseDTO::de).toList(),
                pagamentos.stream().map(TransacaoResponseDTO::de).toList());
    }

    /** Paga (total ou parcialmente) uma fatura, tirando o dinheiro de uma conta. */
    @Transactional
    public TransacaoResponseDTO pagar(UUID cartaoId, PagamentoFaturaRequestDTO dto) {
        Usuario usuario = usuarioLogado.obter();
        CartaoCredito cartao = cartaoDoUsuario(cartaoId); // cartão arquivado também pode ter fatura a pagar
        Conta conta = contaRepository.findByIdAndUsuarioId(dto.contaId(), usuario.getId())
                .filter(Conta::isAtiva)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conta inválida"));

        YearMonth mes = YearMonth.parse(dto.mes());
        LocalDate hoje = LocalDate.now(FUSO);

        Transacao t = new Transacao();
        t.setUsuario(usuario);
        t.setTipo(TipoTransacao.PAGAMENTO_FATURA);
        t.setConta(conta);
        t.setCartao(cartao);
        t.setFaturaMes(mes.atDay(1));
        t.setDescricao("Fatura " + cartao.getNome() + " " + mes.format(MES_ANO));
        t.setValor(dto.valor());
        t.setDataTransacao(dto.data());
        t.setStatus(dto.status() != null ? dto.status()
                : (dto.data().isAfter(hoje) ? StatusTransacao.PENDENTE : StatusTransacao.EFETIVADA));
        return TransacaoResponseDTO.de(transacaoRepository.save(t));
    }

    private CartaoCredito cartaoDoUsuario(UUID id) {
        return cartaoRepository.findByIdAndUsuarioId(id, usuarioLogado.obter().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cartão não encontrado"));
    }

    private BigDecimal soma(Stream<Transacao> transacoes) {
        return transacoes.map(Transacao::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}