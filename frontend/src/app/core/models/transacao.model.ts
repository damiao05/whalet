import { StatusTransacao, TipoTransacao } from './tipo-transacao';

export interface Transacao {
  id: string;
  descricao: string;
  valor: number;
  tipo: TipoTransacao;
  status: StatusTransacao;
  data: string; // 'yyyy-MM-dd'
  contaId: string | null;
  contaDestinoId: string | null;
  cartaoId: string | null;
  categoriaId: string | null;
  parcelaNumero: number | null;
  parcelaTotal: number | null;
  grupoParcelasId: string | null;
  recorrenciaId: string | null;
  faturaMes: string | null;
  metadados: string | null;
}

export interface TransacaoRequest {
  descricao: string;
  valor: number;
  tipo: TipoTransacao;
  data: string;
  contaId: string | null;
  contaDestinoId: string | null;
  cartaoId: string | null;
  categoriaId: string | null;
  status: StatusTransacao | null;
  parcelas: number | null;
  metadados: string | null;
}