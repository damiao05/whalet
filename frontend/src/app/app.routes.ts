import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
import { LayoutComponent } from './layout/layout.component';
import { LoginComponent } from './features/login/login.component';
import { RegistroComponent } from './features/registro/registro.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { ContasComponent } from './features/contas/contas.component';
import { CartoesComponent } from './features/cartoes/cartoes.component';
import { CategoriasComponent } from './features/categorias/categorias.component';
import { LancamentosComponent } from './features/lancamentos/lancamentos.component';
import { FaturaComponent } from './features/fatura/fatura.component';
import { PrivacidadeComponent } from './features/privacidade/privacidade.component';

export const routes: Routes = [
    { path: 'login', component: LoginComponent },
    { path: 'registro', component: RegistroComponent },
    {
        path: '',
        component: LayoutComponent,
        canActivate: [authGuard],
        children: [
            { path: 'dashboard', component: DashboardComponent },
            { path: 'lancamentos', component: LancamentosComponent },
            { path: 'contas', component: ContasComponent },
            { path: 'cartoes', component: CartoesComponent },
            { path: 'cartoes/:id/faturas', component: FaturaComponent },
            { path: 'categorias', component: CategoriasComponent },
            { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
        ]
    },
    { path: 'privacidade', component: PrivacidadeComponent },
    { path: '**', redirectTo: 'dashboard' },
];
