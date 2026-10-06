import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../config/api.config';
import { Medicamento } from '../models/receta.models';

@Injectable({ providedIn: 'root' })
export class MedicamentoService {
  private readonly http = inject(HttpClient);

  listar(): Observable<Medicamento[]> {
    return this.http.get<Medicamento[]>(`${API_URL}/medicamentos`);
  }
}