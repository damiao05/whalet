import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../../environments/environment";
import { Conta, ContaRequest } from "../models/conta.model";

@Injectable({ providedIn: 'root' })
export class ContasService {
    private http = inject(HttpClient);
    private url = `${environment.apiUrl}/contas`;

    listar(): Observable<Conta[]> {
        return this.http.get<Conta[]>(this.url);
    }

    criar(dados: ContaRequest): Observable<Conta> {
        return this.http.post<Conta>(this.url, dados);
    }

    atualizar(id: string, dados: ContaRequest): Observable<Conta> {
        return this.http.put<Conta>(`${this.url}/${id}`, dados);
    }

    arquivar(id: string): Observable<void> {
        return this.http.delete<void>(`${this.url}/${id}`);
    }
}