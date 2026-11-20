import { TipoTransacao } from "./tipo-transacao";

export type Frequencia = 'SEMANAL' | 'MENSAL' | 'ANUAL';

export const FREQUENCIAS: { valor: Frequencia; rotulo: string; unidade: string }[] = [
    { valor: 'MENSAL', rotulo: 'Mensalmente', unidade: 'mês(es)' },
    { valor: 'SEMANAL', rotulo: 'Semanalmente', unidade: 'semana(s)' },
    { valor: 'ANUAL', rotulo: 'Anualmente', unidade: 'ano(s)' },
];

export interface Recorrencia {
  id: string;
  descricao: string;
  valor: number;
  tipo: TipoTransacao;
  contaId: string | null;
  contaDestinoId: string | null;
  cartaoId: string | null;
  categoriaId: string | null;
  frequencia: Frequencia;
  intervalo: number;
  dataInicio: string;
  dataFim: string | null;
  geradoAte: string | null;
  ativa: boolean;
}

export interface RecorrenciaRequest {
  descricao: string;
  valor: number;
  tipo: TipoTransacao;
  contaId: string | null;
  contaDestinoId: string | null;
  cartaoId: string | null;
  categoriaId: string | null;
  frequencia: Frequencia;
  intervalo: number;
  dataInicio: string;
  dataFim: string | null;
}