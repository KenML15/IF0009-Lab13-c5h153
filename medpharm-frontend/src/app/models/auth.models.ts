export type Rol = 'MEDICO' | 'FARMACEUTICO';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  rol: Rol;
}
