import { CurrencyPipe } from '@angular/common';
import { Component, ElementRef, OnInit, computed, inject, signal, viewChild } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Observable, forkJoin } from 'rxjs';
import { Cartao } from '../../core/models/cartao.model';
import { Categoria } from '../../core/models/categoria.model';
import { Conta } from '../../core/models/conta.model';
import { FREQUENCIAS, Frequencia } from '../../core/models/recorrencia.model';
import { StatusTransacao, TipoTransacao } from '../../core/models/tipo-transacao';
import { Transacao, TransacaoRequest } from '../../core/models/transacao.model';
import { CartoesService } from '../../core/services/cartoes.service';
import { CategoriasService } from '../../core/services/categorias.service';
import { ContasService } from '../../core/services/contas.service';
import { RecorrenciasService } from '../../core/services/recorrencias.service';
import { TransacoesService } from '../../core/services/transacoes.service';
import { dataCurta, deslocarMes, hojeIso, mesAtual, rotuloMes } from '../../core/constants/utils/datas';
import { MoneyInputComponent } from '../../shared/money-input/money-input.component';

type Modo = 'UNICA' | 'PARCELADA' | 'RECORRENTE';
type FiltroTipo = 'TODOS' | 'RECEITA' | 'DESPESA' | 'TRANSFERENCIA';
type EscopoExclusao = 'ESTA' | 'PARCELAS' | 'RECORRENCIA';

@Component({
  selector: 'app-lancamentos',
  imports: [ReactiveFormsModule, CurrencyPipe, MoneyInputComponent],
  templateUrl: './lancamentos.component.html',
  styleUrl: './lancamentos.component.css',
})
export class LancamentosComponent implements OnInit {
  private api = inject(TransacoesService);
  private recorrenciasApi = inject(RecorrenciasService);
  private contasApi = inject(ContasService);
  private cartoesApi = inject(CartoesService);
  private categoriasApi = inject(CategoriasService);
  private fb = inject(FormBuilder);

  readonly dataCurta = dataCurta;
  readonly mesDeHoje = mesAtual();
  readonly frequencias = FREQUENCIAS;
  readonly hojeIsoStr = hojeIso();

  readonly tipos: { valor: TipoTransacao; rotulo: string }[] = [
    { valor: 'DESPESA', rotulo: 'Despesa' },
    { valor: 'RECEITA', rotulo: 'Receita' },
    { valor: 'TRANSFERENCIA', rotulo: 'Transferência' },
  ];
  readonly modos: { valor: Modo; rotulo: string }[] = [
    { valor: 'UNICA', rotulo: 'Única' },
    { valor: 'PARCELADA', rotulo: 'Parcelada' },
    { valor: 'RECORRENTE', rotulo: 'Recorrente' },
  ];
  readonly filtrosTipo: { valor: FiltroTipo; rotulo: string }[] = [
    { valor: 'TODOS', rotulo: 'Todos' },
    { valor: 'RECEITA', rotulo: 'Receitas' },
    { valor: 'DESPESA', rotulo: 'Despesas' },
    { valor: 'TRANSFERENCIA', rotulo: 'Transferências' },
  ];

  // ----- dados -----
  mes = signal(mesAtual());
  transacoes = signal<Transacao[]>([]);
  contas = signal<Conta[]>([]);
  cartoes = signal<Cartao[]>([]);
  categorias = signal<Categoria[]>([]);
  carregando = signal(true);
  erro = signal('');

  // ----- filtros -----
  filtroTipo = signal<FiltroTipo>('TODOS');
  filtroOrigem = signal('');
  somentePendentes = signal(false);

  rotulo = computed(() => rotuloMes(this.mes()));

  visiveis = computed(() =>
    this.transacoes().filter((t) => {
      const tipo = this.filtroTipo();
      if (tipo !== 'TODOS' && t.tipo !== tipo) return false;
      if (this.somentePendentes() && t.status !== 'PENDENTE') return false;

      const origem = this.filtroOrigem();
      if (origem) {
        const [tipoOrigem, id] = origem.split(':');
        if (tipoOrigem === 'conta' && t.contaId !== id && t.contaDestinoId !== id) return false;
        if (tipoOrigem === 'cartao' && t.cartaoId !== id) return false;
      }
      return true;
    }),
  );

  /** Transferências e pagamentos de fatura não são receita nem despesa. */
  resumo = computed(() => {
    let receitas = 0, aReceber = 0, despesas = 0, aPagar = 0;
    for (const t of this.transacoes()) {
      if (t.tipo === 'RECEITA') {
        if (t.status === 'EFETIVADA') receitas += t.valor;
        else aReceber += t.valor;
      } else if (t.tipo === 'DESPESA') {
        if (t.status === 'EFETIVADA') despesas += t.valor;
        else aPagar += t.valor;
      }
    }
    return { receitas, aReceber, despesas, aPagar, resultado: receitas - despesas };
  });

  // ----- formulário -----
  dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');
  dlgExcluir = viewChild.required<ElementRef<HTMLDialogElement>>('dlgExcluir');

  tipo = signal<TipoTransacao>('DESPESA');
  modo = signal<Modo>('UNICA');
  editando = signal<Transacao | null>(null);
  excluindo = signal<Transacao | null>(null);
  salvando = signal(false);
  erroForm = signal('');

  form = this.fb.nonNullable.group({
    descricao: ['', [Validators.required, Validators.maxLength(255)]],
    valor: [0, [Validators.required, Validators.min(0.01)]],
    data: [hojeIso(), Validators.required],
    origem: ['', Validators.required], // 'conta:<id>' ou 'cartao:<id>'
    destino: [''],
    categoriaId: [''],
    efetivada: [true],
    parcelas: [2, [Validators.min(2), Validators.max(120)]],
    frequencia: ['MENSAL' as Frequencia],
    intervalo: [1, [Validators.min(1), Validators.max(12)]],
    dataFim: [''],
  });

  /** Espelho do formulário em signal, para os "computed" reagirem ao que o usuário digita. */
  valores = toSignal(this.form.valueChanges, { initialValue: this.form.getRawValue() });

  modosDisponiveis = computed(() =>
    this.tipo() === 'TRANSFERENCIA' ? this.modos.filter((m) => m.valor !== 'PARCELADA') : this.modos,
  );

  opcoesOrigem = computed(() => ({
    contas: this.contas(),
    cartoes: this.tipo() === 'DESPESA' ? this.cartoes() : [],
  }));

  opcoesDestino = computed(() => {
    const origem = this.valores().origem ?? '';
    return this.contas().filter((c) => 'conta:' + c.id !== origem);
  });

  categoriasDoTipo = computed(() => {
    const t = this.tipo();
    return t === 'RECEITA' || t === 'DESPESA'
      ? this.categorias().filter((c) => c.ativa && c.tipo === t)
      : [];
  });

  origemEhCartao = computed(() => (this.valores().origem ?? '').startsWith('cartao:'));

  rotuloValor = computed(() => {
    switch (this.modo()) {
      case 'PARCELADA': return 'Valor total';
      case 'RECORRENTE': return 'Valor de cada ocorrência';
      default: return 'Valor';
    }
  });

  previaParcela = computed(() => {
    const v = this.valores();
    const n = Number(v.parcelas);
    const valor = v.valor ?? 0;
    if (this.modo() !== 'PARCELADA' || !(n >= 2) || !(valor > 0)) return null;
    return valor / n;
  });

  unidade = computed(() => FREQUENCIAS.find((f) => f.valor === this.valores().frequencia)?.unidade ?? '');

  // ----- ciclo de vida e carregamento -----
  ngOnInit(): void {
    this.carregarBase();
    this.carregarMes();
  }

  private carregarBase(): void {
    forkJoin({
      contas: this.contasApi.listar(),
      cartoes: this.cartoesApi.listar(),
      categorias: this.categoriasApi.listar(true), // inclui arquivadas, para mostrar o nome em lançamentos antigos
    }).subscribe({
      next: ({ contas, cartoes, categorias }) => {
        this.contas.set(contas);
        this.cartoes.set(cartoes);
        this.categorias.set(categorias);
      },
      error: () => this.erro.set('Não foi possível carregar contas, cartões e categorias.'),
    });
  }

  carregarMes(): void {
    this.carregando.set(true);
    this.api.listar(this.mes()).subscribe({
      next: (lista) => {
        this.transacoes.set(lista);
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível carregar os lançamentos.');
        this.carregando.set(false);
      },
    });
  }

  mudarMes(delta: number): void {
    this.mes.set(deslocarMes(this.mes(), delta));
    this.carregarMes();
  }

  irParaHoje(): void {
    this.mes.set(mesAtual());
    this.carregarMes();
  }

  // ----- apresentação -----
  private nomeConta(id: string | null): string {
    return id ? (this.contas().find((c) => c.id === id)?.nome ?? 'Conta arquivada') : '';
  }
  private nomeCartao(id: string | null): string {
    return id ? (this.cartoes().find((c) => c.id === id)?.nome ?? 'Cartão arquivado') : '';
  }
  private nomeCategoria(id: string | null): string {
    return id ? (this.categorias().find((c) => c.id === id)?.nome ?? '') : '';
  }

  detalhe(t: Transacao): string {
    switch (t.tipo) {
      case 'TRANSFERENCIA':
        return `${this.nomeConta(t.contaId)} → ${this.nomeConta(t.contaDestinoId)}`;
      case 'PAGAMENTO_FATURA':
        return `${this.nomeConta(t.contaId)} → ${this.nomeCartao(t.cartaoId)}`;
      default: {
        const origem = t.contaId ? this.nomeConta(t.contaId) : this.nomeCartao(t.cartaoId);
        const categoria = this.nomeCategoria(t.categoriaId);
        return categoria ? `${origem} · ${categoria}` : origem;
      }
    }
  }

  corDe(t: Transacao): string {
    if (t.tipo === 'TRANSFERENCIA' || t.tipo === 'PAGAMENTO_FATURA') return 'var(--accent)';
    return this.categorias().find((c) => c.id === t.categoriaId)?.cor ?? '#64748B';
  }

  atrasado(t: Transacao): boolean {
    return t.status === 'PENDENTE' && t.data < hojeIso();
  }

  // ----- status -----
  alternarStatus(t: Transacao): void {
    const novo: StatusTransacao = t.status === 'PENDENTE' ? 'EFETIVADA' : 'PENDENTE';
    // Não dá para ter pago no futuro: ao confirmar uma conta com data futura, a data vira hoje
    const data = novo === 'EFETIVADA' && t.data > hojeIso() ? hojeIso() : undefined;
    this.api.alterarStatus(t.id, novo, data).subscribe({
      next: () => this.carregarMes(),
      error: () => this.erro.set('Não foi possível alterar a situação do lançamento.'),
    });
  }

  podeAlterarStatus(t: Transacao): boolean {
    return !(t.tipo === 'DESPESA' && t.cartaoId);
  }

  // ----- formulário: abrir -----
  novo(): void {
    this.editando.set(null);
    this.tipo.set('DESPESA');
    this.modo.set('UNICA');
    this.form.reset({
      descricao: '', valor: 0, data: hojeIso(), origem: this.origemPadrao(), destino: '',
      categoriaId: '', efetivada: true, parcelas: 2, frequencia: 'MENSAL', intervalo: 1, dataFim: '',
    });
    this.abrir();
  }

  editar(t: Transacao): void {
    this.editando.set(t);
    this.tipo.set(t.tipo);
    this.modo.set('UNICA');
    this.form.reset({
      descricao: t.descricao,
      valor: t.valor,
      data: t.data,
      origem: t.contaId ? `conta:${t.contaId}` : t.cartaoId ? `cartao:${t.cartaoId}` : '',
      destino: t.contaDestinoId ?? '',
      categoriaId: t.categoriaId ?? '',
      efetivada: t.status === 'EFETIVADA',
      parcelas: 2, frequencia: 'MENSAL', intervalo: 1, dataFim: '',
    });
    this.abrir();
  }

  private origemPadrao(): string {
    const primeira = this.contas()[0];
    return primeira ? `conta:${primeira.id}` : '';
  }

  mudarTipo(novo: TipoTransacao): void {
    this.tipo.set(novo);
    this.form.patchValue({ categoriaId: '', destino: '' });
    if (novo !== 'DESPESA' && this.form.controls.origem.value.startsWith('cartao:')) {
      this.form.patchValue({ origem: this.origemPadrao() });
    }
    if (novo === 'TRANSFERENCIA' && this.modo() === 'PARCELADA') {
      this.modo.set('UNICA');
    }
  }

  mudarModo(novo: Modo): void {
    this.modo.set(novo);
  }

  // ----- formulário: salvar -----
  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const tipo = this.tipo();
    if (tipo === 'TRANSFERENCIA' && !v.destino) {
      this.erroForm.set('Escolha a conta de destino.');
      return;
    }

    const [tipoOrigem, idOrigem] = v.origem.split(':');
    const contaId = tipoOrigem === 'conta' ? idOrigem : null;
    const cartaoId = tipoOrigem === 'cartao' ? idOrigem : null;
    const base = {
      descricao: v.descricao.trim(),
      valor: Number(v.valor),
      tipo,
      contaId,
      cartaoId,
      contaDestinoId: tipo === 'TRANSFERENCIA' ? v.destino : null,
      categoriaId: tipo === 'TRANSFERENCIA' ? null : v.categoriaId || null,
    };

    this.salvando.set(true);
    this.erroForm.set('');

    const aoTerminar = {
      next: () => {
        this.salvando.set(false);
        this.fechar();
        this.mes.set(v.data.slice(0, 7)); // leva você ao mês do lançamento
        this.carregarMes();
      },
      error: (e: { error?: { message?: string } }) => {
        this.salvando.set(false);
        this.erroForm.set(e.error?.message || 'Não foi possível salvar. Confira os dados.');
      },
    };

    if (this.modo() === 'RECORRENTE' && !this.editando()) {
      this.recorrenciasApi
        .criar({
          ...base,
          frequencia: v.frequencia,
          intervalo: Number(v.intervalo),
          dataInicio: v.data,
          dataFim: v.dataFim || null,
        })
        .subscribe(aoTerminar);
      return;
    }

    const requisicao: TransacaoRequest = {
      ...base,
      data: v.data,
      // Só contas têm "pago/recebido"; no cartão o backend decide
      status: contaId ? (v.efetivada ? 'EFETIVADA' : 'PENDENTE') : null,
      parcelas: this.modo() === 'PARCELADA' && !this.editando() ? Number(v.parcelas) : null,
      metadados: null,
    };

    const atual = this.editando();
    const chamada: Observable<unknown> = atual
        ? this.api.atualizar(atual.id, requisicao)
        : this.api.criar(requisicao);
    chamada.subscribe(aoTerminar);
  }

  fechar(): void {
    this.dialog().nativeElement.close();
  }

  private abrir(): void {
    this.erroForm.set('');
    this.dialog().nativeElement.showModal();
  }

  // ----- exclusão -----
  pedirExclusao(t: Transacao): void {
    this.excluindo.set(t);
    this.dlgExcluir().nativeElement.showModal();
  }

  fecharExclusao(): void {
    this.dlgExcluir().nativeElement.close();
  }

  excluir(escopo: EscopoExclusao): void {
    const t = this.excluindo();
    if (!t) return;
    const chamada =
      escopo === 'RECORRENCIA' && t.recorrenciaId
        ? this.recorrenciasApi.encerrar(t.recorrenciaId)
        : this.api.excluir(t.id, escopo === 'PARCELAS');

    chamada.subscribe({
      next: () => {
        this.fecharExclusao();
        this.carregarMes();
      },
      error: () => {
        this.fecharExclusao();
        this.erro.set('Não foi possível excluir.');
      },
    });
  }
}