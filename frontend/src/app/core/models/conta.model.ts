export type TipoConta = 'CORRENTE' | 'POUPANCA' | 'DINHEIRO' | 'INVESTIMENTO';

export interface Conta {
    id          : string,
    nome        : string,
    tipo        : TipoConta,
    instituicao : string | null,
    saldoInicial: number;
    saldoAtual  : number;
    ativa       : boolean;
}

export interface ContaRequest {
    nome        : string;
    tipo        : TipoConta,
    instituicao : string | null;
    saldoInicial: number;
}

export const TIPOS_CONTA: { valor: TipoConta, rotulo: string }[] = [
    { valor: 'CORRENTE', rotulo: 'Conta corrente' },
    { valor: 'POUPANCA', rotulo: 'Poupança' },
    { valor: 'DINHEIRO', rotulo: 'Dinheiro' },
    { valor: 'INVESTIMENTO', rotulo: 'Investimento' },
];