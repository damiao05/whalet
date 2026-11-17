import { Component, ElementRef, OnInit, computed, inject, signal, viewChild } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CORES } from '../../core/constants/instituicoes';
import { Categoria, CategoriaRequest } from '../../core/models/categoria.model';
import { TipoTransacao } from '../../core/models/tipo-transacao';
import { CategoriasService } from '../../core/services/categorias.service';

@Component({
  selector: 'app-categorias',
  imports: [ReactiveFormsModule],
  templateUrl: './categorias.component.html',
  styleUrl: './categorias.component.css',
})
export class CategoriasComponent implements OnInit {
  private api = inject(CategoriasService);
  private fb = inject(FormBuilder);

  readonly cores = CORES;

  categorias = signal<Categoria[]>([]);
  despesas = computed(() => this.categorias().filter((c) => c.tipo === 'DESPESA'));
  receitas = computed(() => this.categorias().filter((c) => c.tipo === 'RECEITA'));

  carregando = signal(true);
  salvando = signal(false);
  erro = signal('');
  erroForm = signal('');
  editando = signal<Categoria | null>(null);

  dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(60)]],
    tipo: ['DESPESA' as TipoTransacao, Validators.required],
    cor: [CORES[0]],
  });

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.api.listar().subscribe({
      next: (lista) => {
        this.categorias.set(lista);
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível carregar as categorias.');
        this.carregando.set(false);
      },
    });
  }

  nova(tipo: TipoTransacao): void {
    this.editando.set(null);
    this.form.controls.tipo.enable();
    this.form.reset({ nome: '', tipo, cor: CORES[0] });
    this.abrir();
  }

  editar(c: Categoria): void {
    this.editando.set(c);
    this.form.reset({ nome: c.nome, tipo: c.tipo, cor: c.cor ?? CORES[0] });
    this.form.controls.tipo.disable();
    this.abrir();
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
    const dados: CategoriaRequest = { nome: v.nome.trim(), tipo: v.tipo, cor: v.cor || null };

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
      error: (e) => {
        this.salvando.set(false);
        this.erroForm.set(
          e.status === 409 ? 'Já existe uma categoria com esse nome.' : 'Não foi possível salvar. Confira os dados.',
        );
      },
    });
  }

  arquivar(c: Categoria): void {
    if (!confirm(`Arquivar a categoria "${c.nome}"? Os lançamentos dela continuam guardados.`)) return;
    this.api.arquivar(c.id).subscribe({
      next: () => this.carregar(),
      error: () => this.erro.set('Não foi possível arquivar a categoria.'),
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