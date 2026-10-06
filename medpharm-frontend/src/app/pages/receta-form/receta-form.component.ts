import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import {
  AbstractControl, FormArray, FormControl, FormGroup,
  NonNullableFormBuilder, ReactiveFormsModule, Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Medicamento, RecetaRequest } from '../../models/receta.models';
import { AuthService } from '../../services/auth.service';
import { MedicamentoService } from '../../services/medicamento.service';
import { RecetaService } from '../../services/receta.service';
import { positivoValidator } from '../../validators/positivo.validator';

type DetalleForm = FormGroup<{
  medicamentoId: FormControl<number | null>;
  cantidad: FormControl<number | null>;
  dosisIndicada: FormControl<string>;
}>;

@Component({
  selector: 'app-receta-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './receta-form.component.html',
  styleUrl: './receta-form.component.css',
})
export class RecetaFormComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly recetaService = inject(RecetaService);
  private readonly medicamentoService = inject(MedicamentoService);
  private readonly router = inject(Router);
  readonly auth = inject(AuthService);

  readonly medicamentos = signal<Medicamento[]>([]);
  readonly enviando = signal(false);
  readonly errorServidor = signal<string | null>(null);

  readonly form = this.fb.group({
    pacienteNombre: ['', [Validators.required, Validators.minLength(5)]],
    detalles: this.fb.array<DetalleForm>([this.crearDetalle()]),
  });

  get detalles(): FormArray<DetalleForm> {
    return this.form.controls.detalles;
  }

  ngOnInit(): void {
    this.medicamentoService.listar().subscribe({
      next: data => this.medicamentos.set(data),
      error: () => this.errorServidor.set('No se pudo cargar el catálogo de medicamentos.'),
    });
  }

  // ---------- Manejo del FormArray ----------

  private crearDetalle(): DetalleForm {
    return this.fb.group({
      medicamentoId: this.fb.control<number | null>(null, Validators.required),
      cantidad: this.fb.control<number | null>(1, [Validators.required, positivoValidator]),
      dosisIndicada: this.fb.control('', [Validators.required, Validators.maxLength(255)]),
    });
  }

  agregarDetalle(): void {
    this.detalles.push(this.crearDetalle());
  }

  eliminarDetalle(index: number): void {
    if (this.detalles.length > 1) {
      this.detalles.removeAt(index);
    }
  }

  // ---------- Auxiliares para la plantilla ----------

  mostrarError(control: AbstractControl): boolean {
    return control.invalid && (control.touched || control.dirty);
  }

  stockDisponible(index: number): number | null {
    const id = this.detalles.at(index).controls.medicamentoId.value;
    return this.medicamentos().find(m => m.id === id)?.stock ?? null;
  }

  // ---------- Envío ----------

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valor = this.form.getRawValue();
    const payload: RecetaRequest = {
      pacienteNombre: valor.pacienteNombre.trim(),
      detalles: valor.detalles.map(d => ({
        medicamentoId: d.medicamentoId!,
        cantidad: Number(d.cantidad),
        dosisIndicada: d.dosisIndicada.trim(),
      })),
    };

    this.enviando.set(true);
    this.errorServidor.set(null);

    this.recetaService.crear(payload).subscribe({
      next: () => this.router.navigate(['/recetas']),
      error: (err: HttpErrorResponse) => {
        this.enviando.set(false);
        this.errorServidor.set(
          err.status === 0 ? 'No se pudo conectar con el servidor.'
          : err.status === 403 ? 'Solo los médicos pueden emitir recetas.'
          : err.error?.detail ?? 'No se pudo registrar la receta.'
        );
      },
    });
  }
}