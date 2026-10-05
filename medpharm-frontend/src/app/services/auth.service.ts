import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { API_URL } from '../config/api.config';
import { AuthResponse, LoginRequest } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly STORAGE_KEY = 'medpharm_session';

  private readonly _sesion = signal<AuthResponse | null>(this.cargarSesion());
  readonly sesion = this._sesion.asReadonly();
  readonly isLoggedIn = computed(() => this._sesion() !== null);
  readonly rol = computed(() => this._sesion()?.rol ?? null);

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_URL}/auth/login`, credentials).pipe(
      tap(resp => {
        localStorage.setItem(this.STORAGE_KEY, JSON.stringify(resp));
        this._sesion.set(resp);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.STORAGE_KEY);
    this._sesion.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    const sesion = this._sesion();
    if (!sesion) return null;

    if (this.tokenExpirado(sesion.token)) {
      localStorage.removeItem(this.STORAGE_KEY);
      this._sesion.set(null);
      return null;
    }
    return sesion.token;
  }


  private cargarSesion(): AuthResponse | null {
    try {
      const raw = localStorage.getItem(this.STORAGE_KEY);
      if (!raw) return null;
      const sesion = JSON.parse(raw) as AuthResponse;
      return this.tokenExpirado(sesion.token) ? null : sesion;
    } catch {
      return null;
    }
  }

  private tokenExpirado(token: string): boolean {
    try {
      const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const payload = JSON.parse(atob(base64));
      return payload.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }
}
