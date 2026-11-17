export interface Cartao {
    id: string,
    nome: string,
    bandeira: string | null,
    limite: number,
    diaFechamento: number;
    diaVencimento: number;
    contaPagamentoId: string | null;
    cor: string | null;
    icon: string | null;
    ativo: boolean;
}

export interface CartaoRequest {
    nome: string;
    bandeira: string | null;
    limite: number;
    diaFechamento: number;
    diaVencimento: number;
    contaPagamentoId: string | null;
    cor: string | null;
    icon: string | null;
}

export const BANDEIRAS = ['Visa', 'Mastercard', 'Elo', 'American Express', 'Hipercard', 'Outra'];