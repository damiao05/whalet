import { Component, inject } from "@angular/core";
import { RouterLink, RouterLinkActive, RouterOutlet } from "@angular/router";
import { AuthService } from "../core/services/auth.service";
import { LogoComponent } from "../shared/logo/logo.component";
import { ThemeToggleComponent } from "../shared/theme-toggle/theme-toggle.component";

@Component({
    selector: 'app-layout',
    imports: [RouterOutlet, RouterLink, RouterLinkActive, LogoComponent, ThemeToggleComponent],
    templateUrl: './layout.component.html',
    styleUrl: './layout.component.css',
})
export class LayoutComponent {
    auth = inject(AuthService);

    itens = [
        { rota: '/dashboard', rotulo: 'Início' },
        { rota: '/contas', rotulo: 'Contas' },
        { rota: '/cartoes', rotulo: 'Cartões' },
        { rota: '/categorias', rotulo: 'Categorias' },
    ];
}