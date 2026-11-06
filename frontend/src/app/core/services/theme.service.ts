import { DOCUMENT } from '@angular/common';
import { Injectable, effect, signal, inject, computed } from '@angular/core';

type Tema = 'light' | 'dark' | 'system';
const KEY = 'tema';

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  private document = inject(DOCUMENT);
  
  tema = signal<Tema>(this.lerSalvo());
  private sistemaEscuro = signal(this.sistemaPrefereEscuro());

  efetivo = computed<'light' | 'dark'>(() => {
    const t = this.tema();
    if (t !== 'system') return t;
    return this.sistemaEscuro() ? 'dark' : 'light';
  });

  constructor() {
    if (typeof matchMedia === 'function') {
      matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => this.sistemaEscuro.set(e.matches));
    }
    effect(() => this.aplicar(this.tema()));
  }

  alternar(): void {
    this.tema.set(this.efetivo() === 'dark' ? 'light' : 'dark');
  }

  private aplicar(t: Tema): void {
    const html = this.document.documentElement;
    try {
      if (t === 'system') {
        html.removeAttribute('data-theme');
        localStorage.removeItem(KEY);
      } else {
        html.setAttribute('data-theme', t);
        localStorage.setItem(KEY, t);
      }
    } catch {}
  }

  private lerSalvo(): Tema {
    try {
      const t = localStorage.getItem(KEY);
      return t === 'light' || t === 'dark' ? t : 'system';
    } catch {
      return 'system';
    }
  }

  private sistemaPrefereEscuro(): boolean {
    return typeof matchMedia === 'function' && matchMedia('(prefers-color-scheme: dark)').matches;
  }
}
