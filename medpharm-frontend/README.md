# MedPharm Express — Laboratorio 13

**Universidad de Costa Rica · Sede del Atlántico, Recinto Paraíso**
**Curso:** IF0009 Desarrollo de Software IV · II-2026
**Estudiante:** Kenneth Miranda León · Carné C5H153

Sistema full-stack de gestión de recetas médicas y despacho de farmacia. Los médicos autenticados emiten recetas digitales con varios medicamentos y el personal farmacéutico las consulta, filtra y despacha.

---

## Tecnologías

| Capa | Tecnología |
|---|---|
| Backend | Java 21, Spring Boot 3.5, Spring Data JPA, Spring Security, JJWT 0.12 |
| Base de datos | H2 en memoria (inicializada con `schema.sql` y `data.sql`) |
| Frontend | Angular 19 Standalone, Reactive Forms, Signals, Bootstrap 5 |
| Autenticación | JWT firmado con HMAC-SHA256, sesión stateless |

## Estructura del repositorio

```
IF0009-Lab13-c5h153/
├── medpharm-backend/          API REST Spring Boot
│   └── src/main/java/com/medpharm/
│       ├── config/            PasswordInitializer
│       ├── controller/        AuthController, RecetaController, MedicamentoController
│       ├── dto/               Records de entrada y salida
│       ├── exception/         Excepciones de negocio y GlobalExceptionHandler (RFC 7807)
│       ├── model/             Entidades JPA
│       ├── repository/        Repositorios Spring Data
│       ├── security/          JwtUtils, AuthTokenFilter, AuthEntryPointJwt, WebSecurityConfig
│       └── service/           RecetaService, MedicamentoService
├── medpharm-frontend/         SPA Angular 19
│   └── src/app/
│       ├── guards/            authGuard
│       ├── interceptors/      authInterceptor
│       ├── models/            Interfaces TypeScript
│       ├── pages/             login, recetas-list, receta-form
│       ├── services/          AuthService, RecetaService, MedicamentoService
│       └── validators/        positivoValidator
└── docs/
    └── error_jwt_401.png      Evidencia de la prueba de depuración
```

## Cómo ejecutar

**Requisitos:** JDK 17 o 21, Node.js 20+, Angular CLI 19.

**Backend** (puerto 8080):

```bash
cd medpharm-backend
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

Consola H2: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:medpharmdb`, usuario `sa`, sin contraseña).

**Frontend** (puerto 4200):

```bash
cd medpharm-frontend
npm install
npx ng serve
```

Abrir `http://localhost:4200`.

**Usuarios de prueba** (contraseña `password123`):

| Usuario | Rol | Permisos |
|---|---|---|
| `medico1` | MEDICO | Consultar y emitir recetas |
| `farma1` | FARMACEUTICO | Consultar, despachar y cancelar recetas |

## API REST

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/api/v1/auth/login` | Público | Autentica y devuelve el JWT |
| GET | `/api/v1/recetas` | Autenticado | Lista todas las recetas |
| GET | `/api/v1/recetas/estado/{estado}` | Autenticado | Filtra por PENDIENTE, DESPACHADA o CANCELADA |
| POST | `/api/v1/recetas` | MEDICO | Registra una receta (valida stock disponible) |
| PATCH | `/api/v1/recetas/{id}/estado` | FARMACEUTICO | Despacha o cancela una receta pendiente |
| GET | `/api/v1/medicamentos` | Autenticado | Catálogo para los selectores |

Todos los errores se devuelven con `Content-Type: application/problem+json` según **RFC 7807** (400 validación, 401 no autenticado, 403 sin permiso, 404 no encontrado, 409 regla de negocio).

**Reglas de negocio:** el médico de la receta se toma del token, no del cuerpo de la petición; solo una receta `PENDIENTE` puede cambiar de estado; el stock se valida al emitir y se descuenta al despachar.

---

## Parte 3: Análisis y depuración del error 401

### Procedimiento

1. Se inició sesión normalmente, de modo que el token quedó guardado en `localStorage`.
2. En `app.config.ts` se quitó el registro del interceptor:

   ```typescript
   // Antes
   provideHttpClient(withInterceptors([authInterceptor]))
   // Durante la prueba
   provideHttpClient()
   ```

3. Se navegó a `/recetas`. El guard permitió el acceso (el token sí existe en `localStorage`), pero la petición `GET /api/v1/recetas` falló.

### Evidencia

![Error 401 sin interceptor](docs/error_jwt_401.png)

La pestaña **Network** muestra la petición con estado **401 Unauthorized**. En los *Request Headers* no aparece el encabezado `Authorization`, y el cuerpo de la respuesta es:

```json
{
  "type": "about:blank",
  "title": "No autorizado",
  "status": 401,
  "detail": "Se requiere un token JWT válido en el encabezado Authorization",
  "instance": "/api/v1/recetas"
}
```

### ¿Por qué el servidor rechazó la petición?

La API es **stateless**: no guarda sesiones ni usa cookies (`SessionCreationPolicy.STATELESS`). Cada petición debe demostrar por sí sola quién la envía, y la única forma de hacerlo es el encabezado `Authorization: Bearer <token>`.

El recorrido de la petición en el backend fue:

1. **`AuthTokenFilter`** busca el encabezado `Authorization`. Como no existe, no valida nada y el `SecurityContext` queda vacío (usuario anónimo).
2. **`AuthorizationFilter`** de Spring Security evalúa la regla `anyRequest().authenticated()` para `/api/v1/recetas` y la petición no la cumple.
3. Se lanza una `AuthenticationException`, que **`ExceptionTranslationFilter`** delega en nuestro **`AuthEntryPointJwt`**.
4. `AuthEntryPointJwt` responde **401** con el `ProblemDetail` en formato RFC 7807.

La petición nunca llegó al `RecetaController`.

Es importante notar que el token **sí estaba en el navegador**: guardarlo en `localStorage` no hace que viaje solo. A diferencia de las cookies, el navegador no adjunta automáticamente nada de `localStorage` a las peticiones HTTP; alguien tiene que leerlo y ponerlo en el encabezado.

**401 frente a 403:** el servidor responde 401 cuando no sabe quién es el usuario (falta el token o es inválido) y 403 cuando sí lo sabe pero su rol no tiene permiso. Por ejemplo, `medico1` con un token válido recibe 403 al intentar `PATCH /recetas/{id}/estado`, porque esa acción es exclusiva del rol FARMACEUTICO.

### ¿Cómo lo resuelve el interceptor HTTP?

`authInterceptor` es una función `HttpInterceptorFn` que se ejecuta para **cada** petición que sale por `HttpClient`:

```typescript
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).getToken();
  const esApi = req.url.startsWith(API_URL);
  const esLogin = req.url.includes('/auth/login');

  const peticion = token && esApi && !esLogin
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(peticion);
};
```

1. Obtiene el token desde `AuthService` (que también descarta tokens expirados leyendo el campo `exp`).
2. Como los objetos `HttpRequest` son **inmutables**, usa `req.clone({ setHeaders })` para crear una copia con el encabezado `Authorization: Bearer <token>`.
3. Solo lo agrega a peticiones dirigidas a nuestra API, para no enviar el JWT a dominios externos, y no lo agrega al login.
4. Además, si el servidor responde 401 (token vencido o rechazado), cierra la sesión y redirige a `/login`.

El interceptor solo funciona si está **registrado** en `app.config.ts` con `provideHttpClient(withInterceptors([authInterceptor]))`. Escribir el archivo no basta: si no se registra, Angular nunca lo ejecuta, que es exactamente lo que se simuló en esta prueba.

Al volver a registrarlo, la misma petición muestra `Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...` en los *Request Headers* y responde **200 OK**.

### Observación sobre CORS preflight

Con el interceptor activo, en la pestaña Network aparecen **dos** peticiones por cada llamada a la API: un `OPTIONS` seguido del `GET`. Sin el interceptor, solo aparece el `GET`.

La razón es que el encabezado `Authorization` convierte la petición en una petición "no simple" según CORS. Como el frontend (`localhost:4200`) y el backend (`localhost:8080`) son orígenes distintos, el navegador primero pregunta con `OPTIONS` si el servidor acepta ese encabezado. Para que funcione, `WebSecurityConfig` declara `http://localhost:4200` como origen permitido, incluye `Authorization` en `allowedHeaders` y deja pasar las peticiones `OPTIONS` sin autenticación. Si cualquiera de esas tres cosas faltara, el navegador bloquearía la petición antes de que llegara al controlador.

### Otros problemas encontrados durante el desarrollo

- **Hash BCrypt inválido en `data.sql`:** el hash provisto mide 52 caracteres, pero un hash BCrypt válido mide 60, así que ninguna contraseña coincidía y el login siempre fallaba. Se agregó `PasswordInitializer`, que al arrancar detecta los hashes con formato inválido y los regenera con `BCryptPasswordEncoder`, sin modificar el script original.
- **Versión de Spring Boot:** el generador de proyectos creó el proyecto con Spring Boot 4 (Spring Security 7), que cambia paquetes y nombres de dependencias. Se fijó la versión 3.5 para cumplir con el enunciado.
- **Filtro JWT registrado dos veces:** si `AuthTokenFilter` se anota con `@Component`, Spring Boot lo registra también como filtro global del servidor, fuera de la cadena de Spring Security, y la autenticación se pierde. Por eso se instancia manualmente dentro de `WebSecurityConfig`.