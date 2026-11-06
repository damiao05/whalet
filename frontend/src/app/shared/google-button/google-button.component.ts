import { AfterViewInit, Component, ElementRef, NgZone, effect, inject, input, output, signal, viewChild } from "@angular/core";
import { AuthService } from "../../core/services/auth.service";
import { ThemeService } from "../../core/services/theme.service";
import { environment } from "../../../environments/environment";

type TextoBotao = 'signin_with' | 'signup_with' | 'continue_with';

@Component({
    selector: 'app-google-button',
    template: `<div #btn class="google"></div>`,
    styles: `
        .google { display: flex; justify-content: center; }
        :host ::ng-deep iframe { color-scheme: light; }
    `
})
export class GoogleButtonComponent implements AfterViewInit {
    private auth = inject(AuthService);
    private zone = inject(NgZone);
    private tema = inject(ThemeService);

    texto = input<TextoBotao>('signin_with');
    sucesso = output<void>();
    falhou = output<void>();

    private btn = viewChild.required<ElementRef<HTMLElement>>('btn');
    private pronto = signal(false);

    constructor() {
        effect(() => {
            const escuro = this.tema.efetivo() === 'dark';
            const texto = this.texto();
            if (this.pronto()) this.desenhar(escuro, texto);
        })
    }

    async ngAfterViewInit(): Promise<void> {
        try {
            await this.carregarScript();
        } catch {
            return;
        }

        google.accounts.id.initialize({
            client_id: environment.googleClientId,
            callback: (resp) => this.zone.run(() => this.aoReceberToken(resp.credential)),
        });

        this.pronto.set(true);

    }

    private desenhar(escuro: boolean, texto: TextoBotao): void {
        const el = this.btn().nativeElement;
        el.replaceChildren();

        google.accounts.id.renderButton(el, {
            type: 'standard',
            theme: escuro ? 'filled_black' : 'outline',
            size: 'large',
            text: texto,
            width: 320,
        });
    }

    private aoReceberToken(idToken: string): void {
        this.auth.loginComGoogle(idToken).subscribe({
            next: () => this.sucesso.emit(),
            error: () => this.falhou.emit(),
        });
    }

    private carregarScript(): Promise<void> {
        return new Promise((resolve, reject) => {
            if (typeof google !== 'undefined' && google.accounts) return resolve();
            const s = document.createElement('script');
            s.src = 'https://accounts.google.com/gsi/client';
            s.onload = () => resolve();
            s.onerror = () => reject();
            document.head.appendChild(s);
        })
    }
}