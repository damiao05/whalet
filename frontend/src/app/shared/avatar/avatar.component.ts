import { Component, computed, input } from "@angular/core";

function textoSobre(hex: string): string {
    const n = parseInt(hex.slice(1), 16);
    const r = (n >> 16) & 255, g = (n >> 8) & 255, b = n & 255;
    const brilho = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
    return brilho > 0.6 ? '#0B2239' : '#FFFFFF';
}

@Component({
    selector: 'app-avatar',
    template: `
        <span class="avatar" aria-hidden="true"
            [style.width.px]="tamanho()" [style.height.px]="tamanho()"
            [style.font-size.px]="tamanho() * 0.38"
            [style.background]="fundo()" [style.color]="texto()">{{ iniciais() }}
        </span>
    `,
    styles: `
        .avatar {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
            border-radius: 50%;
            font-weight: 700;
            letter-spacing: .02em;
        }
    `,
})
export class AvatarComponent {
    nome = input.required<string>();
    cor = input<string | null>(null);
    tamanho = input(40);

    fundo = computed(() => this.cor() || '#64748B');
    texto = computed(() => textoSobre(this.fundo()));
    iniciais = computed(() => {
        const partes = this.nome().trim().split(/\s+/).filter(Boolean);
        if (partes.length === 0) return '?';
        if (partes.length === 1) return partes[0].slice(0, 2).toUpperCase();
        return (partes[0][0] + partes[1][0]).toUpperCase();
    });
}