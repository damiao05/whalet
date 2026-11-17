import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Categoria, CategoriaRequest } from '../models/categoria.model';

@Injectable({ providedIn: 'root' })
export class CategoriasService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/categorias`;

  listar(incluirArquivadas = false): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.url, { params: { incluirArquivadas } });
  }
  criar(dados: CategoriaRequest): Observable<Categoria> {
    return this.http.post<Categoria>(this.url, dados);
  }
  atualizar(id: string, dados: CategoriaRequest): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.url}/${id}`, dados);
  }
  arquivar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}