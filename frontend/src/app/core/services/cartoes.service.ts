import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Cartao, CartaoRequest } from '../models/cartao.model';

@Injectable({ providedIn: 'root' })
export class CartoesService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/cartoes`;

  listar(): Observable<Cartao[]> {
    return this.http.get<Cartao[]>(this.url);
  }
  criar(dados: CartaoRequest): Observable<Cartao> {
    return this.http.post<Cartao>(this.url, dados);
  }
  atualizar(id: string, dados: CartaoRequest): Observable<Cartao> {
    return this.http.put<Cartao>(`${this.url}/${id}`, dados);
  }
  arquivar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}