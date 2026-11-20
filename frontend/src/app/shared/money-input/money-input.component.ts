import { Component, ElementRef, forwardRef, input, signal, viewChild } from "@angular/core";
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from "@angular/forms";

const FORMATO = new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const MAX_DIGITOS = 15;

@Component({
    selector: 'app-money-input',
    templateUrl: './money-input.component.html',
    styleUrl: './money-input.component.css',
    providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => MoneyInputComponent), multi: true }],
})
export class MoneyInputComponent implements ControlValueAccessor {
    permitirNegativo = input(false);

    campo = viewChild.required<ElementRef<HTMLInputElement>>('campo');
    texto = signal(FORMATO.format(0));
    negativo = signal(false);
    desabilitado = signal(false);

    private centavos = 0;
    private onChange: (valor: number) => void = () => {};
    onTouched: () => void = () => {};

    writeValue(valor: number | null): void {
        const v = valor ?? 0;
        this.centavos = Math.round(Math.abs(v) * 100);
        this.negativo.set(v < 0);
        this.texto.set(this.formatar());
    }

    registerOnChange(fn: (valor: number) => void): void { this.onChange = fn; }
    registerOnTouched(fn: () => void): void { this.onTouched = fn; }
    setDisabledState(desabilitado: boolean): void { this.desabilitado.set(desabilitado); }

    aoDigitar(evento: Event): void {
        const el = evento.target as HTMLInputElement;
        const bruto = el.value;

        if (this.permitirNegativo()) {
        const antes = this.negativo() ? 1 : 0;
        const agora = (bruto.match(/-/g) ?? []).length;
        if (agora > antes) this.negativo.update((n) => !n);
        }

        const digitos = bruto.replace(/\D/g, '').replace(/^0+/, '');
        if (digitos.length <= MAX_DIGITOS) {
        this.centavos = digitos ? Number(digitos) : 0;
        }

        this.atualizar(el);
    }

    alternarSinal(): void {
        this.negativo.update((n) => !n);
        this.atualizar(this.campo().nativeElement);
    }

    fim(evento: Event): void {
        const el = evento.target as HTMLInputElement;
        el.setSelectionRange(el.value.length, el.value.length);
    }

    private atualizar(el: HTMLInputElement): void {
        const texto = this.formatar();
        this.texto.set(texto);
        el.value = texto;
        el.setSelectionRange(texto.length, texto.length);
        this.onChange(this.valor());
    }

    private valor(): number {
        if (this.centavos === 0) return 0;
        return (this.negativo() ? -1 : 1) * (this.centavos / 100);
    }

    private formatar(): string {
        return (this.negativo() ? '-' : '') + FORMATO.format(this.centavos / 100);
    }
}