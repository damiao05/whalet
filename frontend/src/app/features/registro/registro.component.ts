import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { GoogleButtonComponent } from '../../shared/google-button/google-button.component';

function senhasIguais(group: AbstractControl): ValidationErrors | null {
  const senha = group.get('senha')?.value;
  const confirmar = group.get('confirmarSenha')?.value;
  return senha === confirmar ? null : { senhasDiferentes: true };
}

@Component({
  selector: 'app-registro.component',
  imports: [ReactiveFormsModule, RouterLink, GoogleButtonComponent],
  templateUrl: './registro.component.html',
  styleUrl: './registro.component.css',
})
export class RegistroComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  erro = signal("");
  carregando = signal(false);

  form = this.fb.nonNullable.group(
    {
      primeiroNome: ['', [Validators.required, Validators.maxLength(100)]],
      sobrenome: ['', [Validators.required, Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email]],
      senha: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(72)]],
      confirmarSenha: ['', [Validators.required]],
    },
    { validators: senhasIguais },
  );

  cadastrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.carregando.set(true);
    this.erro.set('');

    const { primeiroNome, sobrenome, email, senha } = this.form.getRawValue();

    this.auth.registrar({ primeiroNome, sobrenome, email, senha }).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (e) => {
        this.carregando.set(false);
        if (e.status === 409) this.erro.set("Este e-mail já está cadastrado");
        else if (e.status === 400) this.erro.set("Dados inválidos, confira os campos");
        else this.erro.set("Não foi possível conectar ao servidor");
      },
    });
  }

  irParaDashboard(): void {
    this.router.navigate(['/dashboard']);
  }
}
