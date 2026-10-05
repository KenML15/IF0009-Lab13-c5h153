import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { LoginComponent } from './pages/login/login.component';
import { RecetasListComponent } from './pages/recetas-list/recetas-list.component';
import { RecetaFormComponent } from './pages/receta-form/receta-form.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent, title: 'Iniciar sesión | MedPharm' },
  { path: 'recetas', component: RecetasListComponent, canActivate: [authGuard], title: 'Recetas | MedPharm' },
  { path: 'nueva-receta', component: RecetaFormComponent, canActivate: [authGuard], title: 'Nueva receta | MedPharm' },
  { path: '', pathMatch: 'full', redirectTo: 'recetas' },
  { path: '**', redirectTo: 'recetas' },
];
