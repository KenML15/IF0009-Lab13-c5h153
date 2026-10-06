import { DatePipe, TitleCasePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { EstadoReceta, Receta } from '../../models/receta.models';
import { AuthService } from '../../services/auth.service';
import { RecetaService } from '../../services/receta.service';

type Filtro = 'TODAS' | EstadoReceta;

@Component({
  selector: 'app-recetas-list',
  imports: [DatePipe, TitleCasePipe, RouterLink],
  templateUrl: './recetas-list.component.html',
  styleUrl: './recetas-list.component.css',
})
export class RecetasListComponent implements OnInit {
  private readonly recetaService = inject(RecetaService);
  readonly auth = inject(AuthService);

  readonly filtros: { valor: Filtro; etiqueta: string }[] = [
    { valor: 'TODAS', etiqueta: 'Todas' },
    { valor: 'PENDIENTE', etiqueta: 'Pendientes' },
    { valor: 'DESPACHADA', etiqueta: 'Despachadas' },
    { valor: 'CANCELADA', etiqueta: 'Canceladas' },
  ];

  // ---------- Estado (signals) ----------
  readonly recetas = signal<Receta[]>([]);
  readonly filtro = signal<Filtro>('TODAS');
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly procesandoId = signal<number | null>(null);

  // ---------- Estado derivado (computed) ----------
  readonly recetasFiltradas = computed(() => {
    const filtro = this.filtro();
    const lista = this.recetas();
    return filtro === 'TODAS' ? lista : lista.filter(r => r.estado === filtro);
  });

  readonly conteo = computed<Record<Filtro, number>>(() => {
    const lista = this.recetas();
    return {
      TODAS: lista.length,
      PENDIENTE: lista.filter(r => r.estado === 'PENDIENTE').length,
      DESPACHADA: lista.filter(r => r.estado === 'DESPACHADA').length,
      CANCELADA: lista.filter(r => r.estado === 'CANCELADA').length,
    };
  });

  readonly esFarmaceutico = computed(() => this.auth.rol() === 'FARMACEUTICO');

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.recetaService.listar().subscribe({
      next: data => {
        this.recetas.set(data);
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(this.mensajeError(err));
        this.cargando.set(false);
      },
    });
  }

  cambiarEstado(receta: Receta, estado: 'DESPACHADA' | 'CANCELADA'): void {
    const accion = estado === 'DESPACHADA' ? 'despachar' : 'cancelar';
    if (!confirm(`¿Desea ${accion} la receta ${receta.codigoReceta}?`)) return;

    this.procesandoId.set(receta.id);
    this.error.set(null);

    this.recetaService.cambiarEstado(receta.id, estado).subscribe({
      next: actualizada => {
        // Reemplaza solo la receta modificada; computed() recalcula filtros y conteos solo
        this.recetas.update(lista => lista.map(r => (r.id === actualizada.id ? actualizada : r)));
        this.procesandoId.set(null);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(this.mensajeError(err));
        this.procesandoId.set(null);
      },
    });
  }

  private mensajeError(err: HttpErrorResponse): string {
    if (err.status === 0) return 'No se pudo conectar con el servidor.';
    if (err.status === 403) return 'No tiene permisos para realizar esta acción.';
    return err.error?.detail ?? 'Ocurrió un error inesperado.';
  }
}