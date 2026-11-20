import { CurrencyPipe } from '@angular/common';
import { Component, ElementRef, OnInit, inject, signal, viewChild } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Cartao } from '../../core/models/cartao.model';
import { Conta } from '../../core/models/conta.model';
import { Fatura, ROTULO_STATUS_FATURA } from '../../core/models/fatura.model';
import { Transacao } from '../../core/models/transacao.model';
import { CartoesService } from '../../core/services/cartoes.service';
import { ContasService } from '../../core/services/contas.service';
import { FaturasService } from '../../core/services/faturas.service';
import { TransacoesService } from '../../core/services/transacoes.service';
import { dataCurta, deslocarMes, hojeIso, rotuloMes } from '../../core/utils/datas';
import { AvatarComponent } from '../../shared/avatar/avatar.component';
import { MoneyInputComponent } from '../../shared/money-input/money-input.component';

@Component({
  selector: 'app-fatura',
  imports: [ReactiveFormsModule, CurrencyPipe, RouterLink, AvatarComponent, MoneyInputComponent],
  templateUrl: './fatura.component.html',
  styleUrl: './fatura.component.css',
})
export class FaturaComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private cartoesApi = inject(CartoesService);
  private contasApi = inject(ContasService);
  private faturasApi = inject(FaturasService);
  private transacoesApi = inject(TransacoesService);
  private fb = inject(FormBuilder);

  readonly dataCurta = dataCurta;
  readonly rotuloStatus = ROTULO_STATUS_FATURA;
  readonly cartaoId = this.route.snapshot.paramMap.get('id') ?? '';

  cartao = signal<Cartao | null>(null);
  contas = signal<Conta[]>([]);
  fatura = signal<Fatura | null>(null);
  carregando = signal(true);
  salvando = signal(false);
  erro = signal('');
  erroForm = signal('');

  dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  form = this.fb.nonNullable.group({
    contaId: ['', Validators.required],
    valor: [0, [Validators.required, Validators.min(0.01)]],
    data: [hojeIso(), Validators.required],
    efetivada: [true],
  });

  ngOnInit(): void {
    forkJoin({ cartoes: this.cartoesApi.listar(), contas: this.contasApi.listar() }).subscribe({
      next: ({ cartoes, contas }) => {
        this.contas.set(contas);
        const cartao = cartoes.find((c) => c.id === this.cartaoId) ?? null;
        this.cartao.set(cartao);
        if (!cartao) {
          this.erro.set('Cartão não encontrado.');
          this.carregando.set(false);
          return;
        }
        this.carregar();
      },
      error: () => {
        this.erro.set('Não foi possível carregar o cartão.');
        this.carregando.set(false);
      },
    });
  }

  carregar(mes?: string): void {
    this.carregando.set(true);
    this.faturasApi.obter(this.cartaoId, mes).subscribe({
      next: (f) => {
        this.fatura.set(f);
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível carregar a fatura.');
        this.carregando.set(false);
      },
    });
  }

  rotuloMes(mes: string): string {
    return rotuloMes(mes);
  }

  mudarMes(delta: number): void {
    const f = this.fatura();
    if (f) this.carregar(deslocarMes(f.mes, delta));
  }

  nomeConta(id: string | null): string {
    return this.contas().find((c) => c.id === id)?.nome ?? 'Conta arquivada';
  }

  abrirPagamento(): void {
    const f = this.fatura();
    if (!f) return;
    const padrao = this.cartao()?.contaPagamentoId ?? this.contas()[0]?.id ?? '';
    this.form.reset({
      contaId: padrao,
      valor: f.emAberto > 0 ? f.emAberto : 0,
      data: hojeIso(),
      efetivada: true,
    });
    this.erroForm.set('');
    this.dialog().nativeElement.showModal();
  }

  pagar(): void {
    const f = this.fatura();
    if (!f) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    this.salvando.set(true);
    this.erroForm.set('');
    this.faturasApi
      .pagar(this.cartaoId, {
        contaId: v.contaId,
        valor: Number(v.valor),
        data: v.data,
        mes: f.mes,
        status: v.efetivada ? 'EFETIVADA' : 'PENDENTE',
      })
      .subscribe({
        next: () => {
          this.salvando.set(false);
          this.fechar();
          this.carregar(f.mes);
        },
        error: (e: { error?: { message?: string } }) => {
          this.salvando.set(false);
          this.erroForm.set(e.error?.message || 'Não foi possível registrar o pagamento.');
        },
      });
  }

  desfazerPagamento(p: Transacao): void {
    if (!confirm('Desfazer este pagamento? O valor volta para a conta e a fatura volta a ficar em aberto.')) return;
    const mes = this.fatura()?.mes;
    this.transacoesApi.excluir(p.id).subscribe({
      next: () => this.carregar(mes),
      error: () => this.erro.set('Não foi possível desfazer o pagamento.'),
    });
  }

  fechar(): void {
    this.dialog().nativeElement.close();
  }
}
