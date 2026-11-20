package controle.financeiro.services;

import java.time.LocalDate;
import java.time.YearMonth;

public final class CicloFatura {

    private CicloFatura() {}

    public static LocalDate dataFechamento(YearMonth mesFechamento, int diaFechamento) {
        return mesFechamento.atDay(Math.min(diaFechamento, mesFechamento.lengthOfMonth()));
    }

    public static LocalDate dataVencimento(YearMonth mesVencimento, int diaVencimento) {
        return mesVencimento.atDay(Math.min(diaVencimento, mesVencimento.lengthOfMonth()));
    }

    /** Compra feita até o dia do fechamento entra na fatura que fecha neste mês; depois, na do mês seguinte. */
    public static YearMonth mesFechamentoDaCompra(LocalDate compra, int diaFechamento) {
        YearMonth mes = YearMonth.from(compra);
        return compra.getDayOfMonth() <= diaFechamento ? mes : mes.plusMonths(1);
    }

    /** Se o vencimento vem depois do fechamento no calendário, vence no mesmo mês; senão, no seguinte. */
    public static YearMonth mesVencimento(YearMonth mesFechamento, int diaFechamento, int diaVencimento) {
        return diaVencimento > diaFechamento ? mesFechamento : mesFechamento.plusMonths(1);
    }

    public static YearMonth mesFechamentoDaFatura(YearMonth mesVencimento, int diaFechamento, int diaVencimento) {
        return diaVencimento > diaFechamento ? mesVencimento : mesVencimento.minusMonths(1);
    }

    /** A fatura (identificada pelo mês de vencimento) em que cai uma compra feita nesta data. */
    public static YearMonth faturaDaCompra(LocalDate compra, int diaFechamento, int diaVencimento) {
        return mesVencimento(mesFechamentoDaCompra(compra, diaFechamento), diaFechamento, diaVencimento);
    }
}