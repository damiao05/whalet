import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Component, ElementRef, OnInit, computed, inject, signal, viewChild } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { CORES, INSTITUICOES, Instituicao } from '../../core/constants/instituicoes';
import { BANDEIRAS, Cartao, CartaoRequest } from '../../core/models/cartao.model';
import { Conta } from '../../core/models/conta.model';
import { CartoesService } from '../../core/services/cartoes.service';
import { ContasService } from '../../core/services/contas.service';
import { AvatarComponent } from '../../shared/avatar/avatar.component';
import { MoneyInputComponent } from '../../shared/money-input/money-input.component';

@Component({
  selector: 'app-cartoes',
  imports: [ReactiveFormsModule, CurrencyPipe, AvatarComponent, MoneyInputComponent, RouterLink],
  templateUrl: './cartoes.component.html',
  styleUrl: './cartoes.component.css',
})
export class CartoesComponent implements OnInit {
  private api = inject(CartoesService);
  private contasApi = inject(ContasService);
  private fb = inject(FormBuilder);

  readonly bandeiras = BANDEIRAS;
  readonly instituicoes = INSTITUICOES;
  readonly cores = CORES;
  readonly dias = Array.from({ length: 31 }, (_, i) => i + 1);

  cartoes = signal<Cartao[]>([]);
  contas = signal<Conta[]>([]);
  carregando = signal(true);
  salvando = signal(false);
  erro = signal('');
  erroForm = signal('');
  editando = signal<Cartao | null>(null);

  limiteTotal = computed(() => this.cartoes().reduce((soma, c) => soma + c.limite, 0));

  dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(100)]],
    bandeira: [''],
    icone: [''],
    cor: [''],
    limite: [0, Validators.required],
    diaFechamento: [25, [Validators.required, Validators.min(1), Validators.max(31)]],
    diaVencimento: [5, [Validators.required, Validators.min(1), Validators.max(31)]],
    contaPagamentoId: [''],
  });

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    forkJoin({ cartoes: this.api.listar(), contas: this.contasApi.listar() }).subscribe({
      next: ({ cartoes, contas }) => {
        this.cartoes.set(cartoes);
        this.contas.set(contas);
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível carregar os cartões.');
        this.carregando.set(false);
      },
    });
  }

  nomeDaConta(id: string | null): string {
    if (!id) return 'Sem conta de pagamento';
    return this.contas().find((c) => c.id === id)?.nome ?? 'Conta arquivada';
  }

  nomeAvatar(icone: string | null, nome: string): string {
    return INSTITUICOES.find((i) => i.chave === icone)?.nome ?? (nome || 'Cartão');
  }

  subtitulo(c: Cartao): string {
    const partes = [c.bandeira, `fecha dia ${c.diaFechamento}`, `vence dia ${c.diaVencimento}`];
    return partes.filter(Boolean).join(' · ');
  }

  novo(): void {
    this.editando.set(null);
    this.form.reset({
      nome: '', bandeira: '', icone: '', cor: '', limite: 0,
      diaFechamento: 25, diaVencimento: 5, contaPagamentoId: '',
    });
    this.abrir();
  }

  editar(c: Cartao): void {
    this.editando.set(c);
    this.form.reset({
      nome: c.nome,
      bandeira: c.bandeira ?? '',
      icone: c.icon ?? '',
      cor: c.cor ?? '',
      limite: c.limite,
      diaFechamento: c.diaFechamento,
      diaVencimento: c.diaVencimento,
      contaPagamentoId: c.contaPagamentoId ?? '',
    });
    this.abrir();
  }

  escolherInstituicao(i: Instituicao): void {
    this.form.patchValue({ icone: i.chave, cor: i.cor });
    if (!this.form.controls.nome.value.trim()) {
      this.form.controls.nome.setValue(`Cartão ${i.nome}`);
    }
  }

  escolherCor(cor: string): void {
    this.form.patchValue({ cor });
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const dados: CartaoRequest = {
      nome: v.nome.trim(),
      bandeira: v.bandeira || null,
      limite: Number(v.limite),
      diaFechamento: Number(v.diaFechamento),
      diaVencimento: Number(v.diaVencimento),
      contaPagamentoId: v.contaPagamentoId || null,
      cor: v.cor || null,
      icon: v.icone || null,
    };

    const atual = this.editando();
    const chamada = atual ? this.api.atualizar(atual.id, dados) : this.api.criar(dados);

    this.salvando.set(true);
    this.erroForm.set('');
    chamada.subscribe({
      next: () => {
        this.salvando.set(false);
        this.fechar();
        this.carregar();
      },
      error: () => {
        this.salvando.set(false);
        this.erroForm.set('Não foi possível salvar. Confira os dados.');
      },
    });
  }

  arquivar(c: Cartao): void {
    if (!confirm(`Arquivar o cartão "${c.nome}"? Os lançamentos dele continuam guardados.`)) return;
    this.api.arquivar(c.id).subscribe({
      next: () => this.carregar(),
      error: () => this.erro.set('Não foi possível arquivar o cartão.'),
    });
  }

  fechar(): void {
    this.dialog().nativeElement.close();
  }

  percentualLimite(c: Cartao): number {
    return c.limite > 0 ? Math.min(100, (c.limiteUsado / c.limite) * 100) : 0;
  }

  private abrir(): void {
    this.erroForm.set('');
    this.dialog().nativeElement.showModal();
  }
}