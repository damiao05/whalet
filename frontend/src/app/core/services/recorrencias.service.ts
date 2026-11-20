import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Recorrencia, RecorrenciaRequest } from '../models/recorrencia.model';

@Injectable({ providedIn: 'root' })
export class RecorrenciasService {
    private http = inject(HttpClient);
    private url = `${environment.apiUrl}/recorrencias`;

    listar(): Observable<Recorrencia[]> {
        return this.http.get<Recorrencia[]>(this.url);
    }

    criar(dados: RecorrenciaRequest): Observable<Recorrencia> {
        return this.http.post<Recorrencia>(this.url, dados);
    }

    atualizar(id: string, dados: RecorrenciaRequest): Observable<Recorrencia> {
        return this.http.put<Recorrencia>(`${this.url}/${id}`, dados);
    }

    encerrar(id: string): Observable<void> {
        return this.http.delete<void>(this.url + '/' + id);
    }
}