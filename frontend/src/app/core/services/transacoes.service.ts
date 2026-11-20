import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../../environments/environment";
import { StatusTransacao } from "../models/tipo-transacao";
import { Transacao, TransacaoRequest } from "../models/transacao.model";

@Injectable({ providedIn: 'root' })
export class TransacoesService {
    private http = inject(HttpClient);
    private url = `${environment.apiUrl}/transacoes`;

    listar(mes: string): Observable<Transacao[]> {
        return this.http.get<Transacao[]>(this.url, { params: { mes } });
    }

    criar(dados: TransacaoRequest): Observable<Transacao[]> {
        return this.http.post<Transacao[]>(this.url, dados);
    }

    atualizar(id: string, dados: TransacaoRequest): Observable<Transacao> {
        return this.http.put<Transacao>(`${this.url}/${id}`, dados);
    }

    alterarStatus(id: string, status: StatusTransacao, data?: string): Observable<Transacao> {
        return this.http.patch<Transacao>(`${this.url}/${id}/status`, { status, data: data ?? null });
    }

    excluir(id: string, todasParcelas = false): Observable<void> {
        return this.http.delete<void>(`${this.url}/${id}`, { params: { todasParcelas } });
    }
}