import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { GoogleButtonComponent } from '../../shared/google-button/google-button.component';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, GoogleButtonComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  erro = signal('');
  carregando = signal(false);

  demorando = signal(false);
  private temporizador?: ReturnType<typeof setTimeout>;

  private iniciarAviso(): void {
    this.temporizador = setTimeout(() => this.demorando.set(true), 4000);
  }

  private pararAviso(): void {
    clearTimeout(this.temporizador);
    this.demorando.set(false);
  }

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required, Validators.minLength(6)]],
  });

  entrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.carregando.set(true);
    this.iniciarAviso();
    this.erro.set('');

    const { email, senha } = this.form.getRawValue();
    this.auth.login(email, senha).subscribe({
      next: () => {
        this.pararAviso();
        this.irParaDashboard();
      },
      error: (e) => {
        this.carregando.set(false);
        this.pararAviso();
        this.erro.set(e.status === 401 ? 'E-mail ou senha inválidos' : 'Não foi possível conectar ao servidor');
      },
    });
  }

  irParaDashboard(): void {
    this.router.navigate(['/dashboard']);
  }
}
