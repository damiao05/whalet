import { CurrencyPipe } from '@angular/common';
import { Component, ElementRef, OnInit, computed, inject, signal, viewChild } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CORES, INSTITUICOES, Instituicao } from '../../core/constants/instituicoes';
import { Conta, ContaRequest, TIPOS_CONTA, TipoConta } from '../../core/models/conta.model';
import { ContasService } from '../../core/services/contas.service';
import { AvatarComponent } from '../../shared/avatar/avatar.component';
import { MoneyInputComponent } from '../../shared/money-input/money-input.component';

@Component({
  selector: 'app-contas',
  imports: [ReactiveFormsModule, CurrencyPipe, AvatarComponent, MoneyInputComponent],
  templateUrl: './contas.component.html',
  styleUrl: './contas.component.css',
})
export class ContasComponent implements OnInit {
  private api = inject(ContasService);
  private fb = inject(FormBuilder);

  readonly tipos = TIPOS_CONTA;
  readonly instituicoes = INSTITUICOES;
  readonly cores = CORES;

  contas = signal<Conta[]>([]);
  carregando = signal(true);
  salvando = signal(false);
  erro = signal('');
  erroForm = signal('');
  editando = signal<Conta | null>(null);

  total = computed(() => this.contas().reduce((soma, c) => soma + c.saldoAtual, 0));

  dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(100)]],
    tipo: ['CORRENTE' as TipoConta, Validators.required],
    instituicao: ['', Validators.maxLength(100)],
    icone: [''],
    cor: [''],
    saldoInicial: [0, Validators.required],
  });

  ngOnInit(): void {
    this.carregar();
  }

  rotulo(tipo: TipoConta): string {
    return this.tipos.find((t) => t.valor === tipo)?.rotulo ?? tipo;
  }

  subtitulo(c: Conta): string {
    const tipo = this.rotulo(c.tipo);
    return c.instituicao ? `${tipo} · ${c.instituicao}` : tipo;
  }

  carregar(): void {
    this.carregando.set(true);
    this.api.listar().subscribe({
      next: (lista) => {
        this.contas.set(lista);
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível carregar as contas.');
        this.carregando.set(false);
      },
    });
  }

  nova(): void {
    this.editando.set(null);
    this.form.reset({ nome: '', tipo: 'CORRENTE', instituicao: '', icone: '', cor: '', saldoInicial: 0 });
    this.abrir();
  }

  editar(conta: Conta): void {
    this.editando.set(conta);
    this.form.reset({
      nome: conta.nome,
      tipo: conta.tipo,
      instituicao: conta.instituicao ?? '',
      icone: conta.icone ?? '',
      cor: conta.cor ?? '',
      saldoInicial: conta.saldoInicial,
    });
    this.abrir();
  }

  escolherInstituicao(i: Instituicao): void {
    this.form.patchValue({ instituicao: i.nome, icone: i.chave, cor: i.cor });
  }

  escolherCor(cor: string): void {
    this.form.patchValue({ cor });
  }

  aoDigitarInstituicao(): void {
    this.form.controls.icone.setValue('');
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const dados: ContaRequest = {
      nome: v.nome.trim(),
      tipo: v.tipo,
      instituicao: v.instituicao.trim() || null,
      icone: v.icone || null,
      cor: v.cor || null,
      saldoInicial: Number(v.saldoInicial),
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

  arquivar(conta: Conta): void {
    if (!confirm(`Arquivar a conta "${conta.nome}"? Os lançamentos dela continuam guardados.`)) return;
    this.api.arquivar(conta.id).subscribe({
      next: () => this.carregar(),
      error: () => this.erro.set('Não foi possível arquivar a conta.'),
    });
  }

  fechar(): void {
    this.dialog().nativeElement.close();
  }

  private abrir(): void {
    this.erroForm.set('');
    this.dialog().nativeElement.showModal();
  }
}