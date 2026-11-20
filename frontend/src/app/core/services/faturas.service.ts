import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Fatura, PagamentoFaturaRequest } from '../models/fatura.model';
import { Transacao } from '../models/transacao.model';

@Injectable({ providedIn: 'root' })
export class FaturasService {
  private http = inject(HttpClient);

  /** Sem "mes", o backend devolve a fatura em que uma compra de hoje cairia. */
  obter(cartaoId: string, mes?: string): Observable<Fatura> {
    const params: Record<string, string> = mes ? { mes } : {};
    return this.http.get<Fatura>(`${environment.apiUrl}/cartoes/${cartaoId}/faturas`, { params });
  }

  pagar(cartaoId: string, dados: PagamentoFaturaRequest): Observable<Transacao> {
    return this.http.post<Transacao>(`${environment.apiUrl}/cartoes/${cartaoId}/pagamentos`, dados);
  }
}
