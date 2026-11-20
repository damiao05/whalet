import { TipoTransacao } from "./tipo-transacao";

export interface Categoria {
    id: string;
    nome: string;
    tipo: TipoTransacao;
    cor: string | null;
    ativa: boolean;
}

export interface CategoriaRequest {
    nome: string;
    tipo: TipoTransacao;
    cor: string | null;
}