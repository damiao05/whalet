export type TipoCategoria = 'RECEITA' | 'DESPESA';
export type TipoTransacao = TipoCategoria | 'TRANSFERENCIA' | 'PAGAMENTO_FATURA';
export type StatusTransacao = 'PENDENTE' | 'EFETIVADA';