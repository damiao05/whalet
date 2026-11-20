import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin, map, of, switchMap } from 'rxjs';
import { Cartao } from '../../core/models/cartao.model';
import { Categoria } from '../../core/models/categoria.model';
import { Conta } from '../../core/models/conta.model';
import { Fatura } from '../../core/models/fatura.model';
import { Transacao } from '../../core/models/transacao.model';
import { CartoesService } from '../../core/services/cartoes.service';
import { CategoriasService } from '../../core/services/categorias.service';
import { ContasService } from '../../core/services/contas.service';
import { FaturasService } from '../../core/services/faturas.service';
import { TransacoesService } from '../../core/services/transacoes.service';
import { dataCurta, deslocarDias, deslocarMes, hojeIso, mesAtual, rotuloMes } from '../../core/utils/datas';

interface ItemVencimento {
  chave: string;
  titulo: string;
  detalhe: string;
  data: string;
  valor: number;
  sinal: 1 | -1 | 0;
  atrasado: boolean;
  link: string;
}

interface GastoCategoria {
  nome: string;
  cor: string;
  total: number;
  percentual: number;
}

const JANELA_DIAS = 14;

@Component({
  selector: 'app-dashboard',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent implements OnInit {
  private contasApi = inject(ContasService);
  private cartoesApi = inject(CartoesService);
  private categoriasApi = inject(CategoriasService);
  private transacoesApi = inject(TransacoesService);
  private faturasApi = inject(FaturasService);

  readonly dataCurta = dataCurta;
  readonly janela = JANELA_DIAS;
  readonly mes = mesAtual();
  readonly rotulo = rotuloMes(mesAtual());

  carregando = signal(true);
  erro = signal('');

  contas = signal<Conta[]>([]);
  cartoes = signal<Cartao[]>([]);
  categorias = signal<Categoria[]>([]);
  transacoes = signal<Transacao[]>([]);
  pendentes = signal<Transacao[]>([]);
  faturas = signal<Fatura[]>([]);

  saldoTotal = computed(() => this.contas().reduce((s, c) => s + c.saldoAtual, 0));
  limiteDisponivel = computed(() => this.cartoes().reduce((s, c) => s + c.limiteDisponivel, 0));

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

  gastos = computed<GastoCategoria[]>(() => {
    const porCategoria = new Map<string, number>();
    for (const t of this.transacoes()) {
      if (t.tipo !== 'DESPESA') continue;
      const chave = t.categoriaId ?? '';
      porCategoria.set(chave, (porCategoria.get(chave) ?? 0) + t.valor);
    }
    const total = [...porCategoria.values()].reduce((s, v) => s + v, 0);
    return [...porCategoria.entries()]
      .map(([id, valor]) => {
        const cat = this.categorias().find((c) => c.id === id);
        return {
          nome: cat?.nome ?? 'Sem categoria',
          cor: cat?.cor ?? '#64748B',
          total: valor,
          percentual: total > 0 ? (valor / total) * 100 : 0,
        };
      })
      .sort((a, b) => b.total - a.total);
  });

  vencimentos = computed<ItemVencimento[]>(() => {
    const hoje = hojeIso();
    const limite = deslocarDias(hoje, JANELA_DIAS);
    const itens: ItemVencimento[] = [];

    for (const t of this.pendentes()) {
      itens.push({
        chave: 't' + t.id,
        titulo: t.descricao,
        detalhe: t.tipo === 'RECEITA' ? 'A receber' : t.tipo === 'TRANSFERENCIA' ? 'Transferência agendada' : 'A pagar',
        data: t.data,
        valor: t.valor,
        sinal: t.tipo === 'RECEITA' ? 1 : t.tipo === 'DESPESA' ? -1 : 0,
        atrasado: t.data < hoje,
        link: '/lancamentos',
      });
    }

    for (const f of this.faturas()) {
      if (f.emAberto <= 0.005 || f.dataVencimento > limite) continue;
      const cartao = this.cartoes().find((c) => c.id === f.cartaoId);
      itens.push({
        chave: 'f' + f.cartaoId + f.mes,
        titulo: 'Fatura ' + (cartao?.nome ?? 'do cartão'),
        detalhe: f.status === 'ABERTA' ? 'Fatura aberta' : 'Fatura fechada',
        data: f.dataVencimento,
        valor: f.emAberto,
        sinal: -1,
        atrasado: f.dataVencimento < hoje,
        link: '/cartoes/' + f.cartaoId + '/faturas',
      });
    }
    return itens.sort((a, b) => a.data.localeCompare(b.data));
  });

  ngOnInit(): void {
    const hoje = hojeIso();
    forkJoin({
      contas: this.contasApi.listar(),
      cartoes: this.cartoesApi.listar(),
      categorias: this.categoriasApi.listar(true),
      transacoes: this.transacoesApi.listar(this.mes),
      pendentes: this.transacoesApi.pendentes(deslocarDias(hoje, JANELA_DIAS)),
    })
      .pipe(
        switchMap((dados) => {
          const faturas$ = dados.cartoes.length
            ? forkJoin(
                dados.cartoes.map((c) =>
                  this.faturasApi.obter(c.id).pipe(
                    switchMap((atual) =>
                      this.faturasApi
                        .obter(c.id, deslocarMes(atual.mes, -1))
                        .pipe(map((anterior): Fatura[] => [atual, anterior])),
                    ),
                  ),
                ),
              ).pipe(map((pares) => pares.flat()))
            : of<Fatura[]>([]);
          return faturas$.pipe(map((faturas) => ({ ...dados, faturas })));
        }),
      )
      .subscribe({
        next: (d) => {
          this.contas.set(d.contas);
          this.cartoes.set(d.cartoes);
          this.categorias.set(d.categorias);
          this.transacoes.set(d.transacoes);
          this.pendentes.set(d.pendentes);
          this.faturas.set(d.faturas);
          this.carregando.set(false);
        },
        error: () => {
          this.erro.set('Não foi possível carregar o painel.');
          this.carregando.set(false);
        },
      });
  }

  percentualLimite(c: Cartao): number {
    return c.limite > 0 ? Math.min(100, (c.limiteUsado / c.limite) * 100) : 0;
  }
}
