import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-privacidade',
  imports: [RouterLink],
  template: `
    <main class="texto">
      <a routerLink="/login">‹ Voltar</a>
      <h1>Política de privacidade</h1>
      <p><strong>Whalet</strong> é um aplicativo pessoal de controle financeiro.</p>

      <h2>Quais dados guardamos</h2>
      <p>Nome, e-mail, a senha (somente em forma criptografada) e os dados financeiros que você mesmo cadastra:
        contas, cartões, categorias e lançamentos. Se você entra com o Google, recebemos apenas nome e e-mail.</p>

      <h2>Para que usamos</h2>
      <p>Exclusivamente para mostrar e organizar as suas finanças dentro do aplicativo. Não vendemos nem
        compartilhamos seus dados, e não os usamos para publicidade.</p>

      <h2>Onde ficam</h2>
      <p>Em serviços de hospedagem de terceiros (banco de dados, servidor e site), acessados por conexão segura.</p>

      <!-- <h2>Seus direitos</h2>
      <p>Você pode pedir a correção ou a exclusão da sua conta e dos seus dados a qualquer momento, pelo e-mail
        <strong>SEU-EMAIL-AQUI</strong>.</p> -->
    </main>
  `,
  styles: `
    .texto { max-width: 680px; margin: 5vh auto; padding: 1.5rem; line-height: 1.6; }
    h2 { font-size: 1.1rem; margin-top: 1.5rem; }
    a { color: var(--primary); }
  `,
})
export class PrivacidadeComponent {}
