import { StatusTransacao } from './tipo-transacao';
import { Transacao } from './transacao.model';

export type StatusFatura = 'ABERTA' | 'FECHADA' | 'VENCIDA' | 'PAGA';

export interface Fatura {
  cartaoId: string;
  mes: string;
  dataInicio: string;
  dataFechamento: string;
  dataVencimento: string;
  total: number;
  pago: number;
  emAberto: number;
  status: StatusFatura;
  compras: Transacao[];
  pagamentos: Transacao[];
}

export interface PagamentoFaturaRequest {
  contaId: string;
  valor: number;
  data: string;
  mes: string;
  status: StatusTransacao | null;
}

export const ROTULO_STATUS_FATURA: Record<StatusFatura, string> = {
  ABERTA: 'Aberta',
  FECHADA: 'Fechada',
  VENCIDA: 'Vencida',
  PAGA: 'Paga',
};
