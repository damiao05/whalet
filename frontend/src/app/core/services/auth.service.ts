import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AuthResponse {
  token: string,
  nome: string,
  email: string
}

export interface RegistroRequest {
  primeiroNome: string;
  sobrenome: string;
  email: string;
  senha: string;
}

const TOKEN_KEY = 'token';
const NOME_KEY = 'nome';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  login(email: string, senha: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/login`, { email, senha })
      .pipe(tap((resp) => this.salvarSessao(resp)));
  }

  loginComGoogle(idToken: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/google`, { idToken })
      .pipe(tap((resp) => this.salvarSessao(resp)));
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(NOME_KEY);
    this.router.navigate(['/login']);
  }

  get token(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  estaLogado(): boolean {
    return !!this.token;
  }

  registrar(dados: RegistroRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/registro`, dados)
      .pipe(tap((resp) => this.salvarSessao(resp)));
  }

  private salvarSessao(resp: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, resp.token);
    localStorage.setItem(NOME_KEY, resp.nome);
  }
}
