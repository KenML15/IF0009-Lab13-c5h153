import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../config/api.config';
import { EstadoReceta, Receta, RecetaRequest } from '../models/receta.models';

@Injectable({ providedIn: 'root' })
export class RecetaService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/recetas`;

  listar(): Observable<Receta[]> {
    return this.http.get<Receta[]>(this.url);
  }

  listarPorEstado(estado: EstadoReceta): Observable<Receta[]> {
    return this.http.get<Receta[]>(`${this.url}/estado/${estado}`);
  }

  crear(receta: RecetaRequest): Observable<Receta> {
    return this.http.post<Receta>(this.url, receta);
  }

  cambiarEstado(id: number, estado: 'DESPACHADA' | 'CANCELADA'): Observable<Receta> {
    return this.http.patch<Receta>(`${this.url}/${id}/estado`, { estado });
  }
}