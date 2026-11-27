# API

Base local: `http://localhost:8080`. Documentación interactiva: `http://localhost:8080/swagger-ui.html`.

## Convenciones

- JSON en `camelCase`.
- Fechas: `LocalDate` como `YYYY-MM-DD`; fecha y hora con offset ISO-8601 (`2026-11-24T09:00:00-03:00`).
- El *access token* se envía como `Authorization: Bearer <token>`.
- El *refresh token* viaja en la cookie `refresh_token` (el frontend debe usar `credentials: 'include'`).
- Errores con formato Problem Details:

```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "Ese horario acaba de ser reservado. Elegí otro.",
  "instance": "/turnos",
  "codigo": "CONFLICTO"
}
```

Códigos habituales: `SOLICITUD_INVALIDA` (400), `NO_AUTENTICADO` (401), `PROHIBIDO` (403), `NO_ENCONTRADO` (404), `CONFLICTO` (409), `DEMASIADAS_SOLICITUDES` (429).

## Endpoints públicos

### `GET /especialidades`

Lista las especialidades activas.

```json
[
  { "id": "uuid", "nombre": "Clínica Médica", "duracionDefaultMin": 30, "activo": true }
]
```

### `GET /obras-sociales`

Lista obras sociales activas con sus planes.

```json
[
  {
    "id": "uuid",
    "nombre": "OSDE",
    "codigo": "OSDE",
    "activo": true,
    "planes": [{ "id": "uuid", "nombre": "Plan 210", "codigo": "210", "activo": true }]
  }
]
```

### `GET /medicos`

Busca médicos activos. Parámetros opcionales: `especialidadId`, `obraSocialId`, `planId`.

```bash
curl "http://localhost:8080/medicos?especialidadId=<uuid>&obraSocialId=<uuid>&planId=<uuid>"
```

### `GET /medicos/{id}`

Perfil completo con especialidades y coberturas aceptadas.

### `GET /medicos/{id}/slots?fecha=YYYY-MM-DD`

Slots libres del día. Si se omite `fecha`, usa hoy.

```json
[
  {
    "fechaHora": "2026-11-24T09:00:00-03:00",
    "consultorioId": "uuid",
    "consultorio": "Consultorio 1",
    "duracionMin": 30
  }
]
```

Solo devuelve horarios con al menos 2 horas de anticipación y hasta 90 días adelante. No incluye turnos ocupados, excepciones ni horarios fuera de la disponibilidad.

## Autenticación

### `POST /auth/staff/login`

Login de administradores y médicos.

```json
{ "email": "nicodepor3.1@gmail.com", "password": "admin1234" }
```

Respuesta `200`:

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "usuario": {
    "id": "uuid",
    "email": "nicodepor3.1@gmail.com",
    "nombre": "Administrador",
    "apellido": "General",
    "rol": "ADMIN"
  }
}
```

Además setea la cookie `refresh_token`.

### `POST /auth/paciente/otp`

Solicita un OTP. Si el DNI no existe, crea el paciente (requiere `nombre`, `email`, `telefono` y `consentimiento: true`).

```json
{
  "dni": "30111222",
  "apellido": "Paciente",
  "nombre": "Ana",
  "email": "ana@example.com",
  "telefono": "1122334455",
  "fechaNacimiento": "1990-05-20",
  "obraSocialId": "uuid",
  "planId": "uuid",
  "numeroAfiliado": "123456",
  "consentimiento": true
}
```

Respuesta `200`:

```json
{
  "mensaje": "Te enviamos un código a a***@example.com.",
  "expiraEnMinutos": 10,
  "emailEnmascarado": "a***@example.com"
}
```

Límites: 3 envíos cada 15 minutos, código de 6 dígitos válido 10 minutos, 5 intentos.

### `POST /auth/paciente/otp/verificar`

```json
{ "dni": "30111222", "codigo": "123456" }
```

Devuelve el mismo `TokenResponse` que el login de staff y setea la cookie de refresh.

### `POST /auth/refresh`

Rota el refresh token usando la cookie. No requiere body.

### `POST /auth/logout`

Revoca el refresh token y limpia la cookie.

### `GET /auth/me`

Devuelve el usuario autenticado. Requiere access token.

## Paciente autenticado

Rol requerido: `PACIENTE`.

### `GET /pacientes/me`

Datos del paciente y su cobertura.

### `PUT /pacientes/me`

```json
{
  "telefono": "1122334455",
  "obraSocialId": "uuid",
  "planId": "uuid",
  "numeroAfiliado": "123456"
}
```

## Turnos

Rol requerido: `PACIENTE`.

### `POST /turnos`

```json
{
  "medicoId": "uuid",
  "fechaHora": "2026-11-24T09:00:00-03:00",
  "tipoCobertura": "OBRA_SOCIAL",
  "motivo": "Control anual"
}
```

- `tipoCobertura` puede ser `OBRA_SOCIAL`, `PARTICULAR` o `null`.
- Si es `null`, el sistema usa la cobertura del paciente cuando el médico la acepta; si no, crea un turno particular con el precio del médico.
- Si se pide `OBRA_SOCIAL` y el médico no la acepta, responde `409`.

Respuesta `200`:

```json
{
  "id": "uuid",
  "medicoId": "uuid",
  "medico": "Laura Gómez",
  "especialidades": "Clínica Médica",
  "pacienteId": "uuid",
  "paciente": "Ana Paciente",
  "pacienteDni": "30111222",
  "fechaHora": "2026-11-24T09:00:00-03:00",
  "duracionMin": 30,
  "consultorio": "Consultorio 1",
  "estado": "CONFIRMADO",
  "tipoCobertura": "OBRA_SOCIAL",
  "obraSocial": "OSDE",
  "plan": "Plan 210",
  "numeroAfiliado": "123456",
  "precio": null,
  "motivo": "Control anual",
  "cancelable": true,
  "creadoEn": "2026-11-22T10:15:30Z"
}
```

### `GET /turnos/mios`

Lista todos los turnos del paciente, del más reciente al más antiguo. El campo `cancelable` indica si todavía se puede cancelar o reprogramar (más de 24 horas de anticipación y estado activo).

### `PATCH /turnos/{id}/cancelar`

```json
{ "motivo": "No puedo asistir" }
```

Solo hasta 24 horas antes. Estado resultante: `CANCELADO_PACIENTE`.

### `PATCH /turnos/{id}/reprogramar`

```json
{ "fechaHora": "2026-11-25T10:30:00-03:00" }
```

Valida el nuevo slot, actualiza consultorio y duración, y envía email de reprogramación.

## Panel del médico

Rol requerido: `MEDICO`.

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/medico/perfil` | Perfil profesional |
| GET | `/medico/agenda` | Perfil + disponibilidad + excepciones |
| GET | `/medico/turnos?fecha=YYYY-MM-DD` | Turnos del día (por defecto hoy) |

## Administración

Rol requerido: `ADMIN`. Todas las rutas cuelgan de `/admin`.

### Médicos y especialidades

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/admin/medicos` | Lista médicos |
| GET | `/admin/medicos/{id}` | Detalle |
| POST | `/admin/medicos` | Crea médico y su usuario |
| PUT | `/admin/medicos/{id}` | Actualiza datos, especialidades y coberturas |
| PATCH | `/admin/medicos/{id}/estado?activo=true` | Activa o desactiva |
| GET | `/admin/especialidades` | Lista todas |
| POST | `/admin/especialidades` | Crea |
| PUT | `/admin/especialidades/{id}` | Actualiza |

`POST /admin/medicos`:

```json
{
  "nombre": "Laura",
  "apellido": "Gómez",
  "email": "laura.gomez@clinica.local",
  "password": "unaClaveSegura",
  "matricula": "MP-12345",
  "duracionTurnoMin": 30,
  "precioParticular": 15000,
  "biografia": "Clínica médica",
  "especialidadIds": ["uuid"],
  "coberturas": [
    { "obraSocialId": "uuid", "planId": null },
    { "obraSocialId": "uuid", "planId": "uuid" }
  ]
}
```

`PUT /admin/medicos/{id}` acepta los mismos campos, `password` opcional y `activo`.

### Coberturas

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/admin/obras-sociales` | Lista todas, incluidas inactivas |
| POST | `/admin/obras-sociales` | Crea obra social |
| PUT | `/admin/obras-sociales/{id}` | Actualiza |
| PATCH | `/admin/obras-sociales/{id}/estado?activo=true` | Activa o desactiva |
| POST | `/admin/obras-sociales/{id}/planes` | Crea plan |
| PUT | `/admin/planes/{id}` | Actualiza plan |

### Agenda

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/admin/consultorios` | Lista consultorios |
| POST | `/admin/consultorios` | Crea consultorio |
| PUT | `/admin/consultorios/{id}` | Actualiza |
| GET | `/admin/medicos/{medicoId}/disponibilidad` | Franjas del médico |
| POST | `/admin/medicos/{medicoId}/disponibilidad` | Crea franja |
| PUT | `/admin/disponibilidad/{id}` | Actualiza franja |
| PATCH | `/admin/disponibilidad/{id}/estado?activo=true` | Activa o desactiva |
| GET | `/admin/medicos/{medicoId}/excepciones` | Licencias y bloqueos |
| POST | `/admin/medicos/{medicoId}/excepciones` | Crea excepción |
| DELETE | `/admin/excepciones/{id}` | Elimina excepción |

`POST /admin/medicos/{medicoId}/disponibilidad`:

```json
{
  "consultorioId": "uuid",
  "diaSemana": 2,
  "horaDesde": "09:00",
  "horaHasta": "13:00",
  "vigenteDesde": "2026-11-01",
  "vigenteHasta": "2027-03-01"
}
```

`diaSemana`: 1 = lunes, 7 = domingo.

`POST /admin/medicos/{medicoId}/excepciones`:

```json
{
  "fechaDesde": "2026-12-24",
  "fechaHasta": "2026-12-31",
  "todoElDia": true,
  "motivo": "Vacaciones"
}
```

Para un bloqueo parcial, `todoElDia: false` con `horaDesde` y `horaHasta`.

### Turnos

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/admin/turnos?fecha=&medicoId=&estado=&page=0&size=20` | Lista paginada |
| GET | `/admin/turnos/{id}` | Detalle |
| PATCH | `/admin/turnos/{id}/estado` | Cambia a `ATENDIDO` o `AUSENTE` |
| PATCH | `/admin/turnos/{id}/cancelar?motivo=...` | Cancela desde la clínica |

`PATCH /admin/turnos/{id}/estado`:

```json
{ "estado": "ATENDIDO", "motivo": null }
```

### Pacientes

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/admin/pacientes?texto=&page=0&size=20` | Busca por nombre, apellido, DNI o email |
| GET | `/admin/pacientes/{id}` | Detalle |
| PUT | `/admin/pacientes/{id}` | Actualiza datos y cobertura |

## Ejemplo de flujo completo con curl

```bash
# 1. Buscar médicos de clínica médica
curl "http://localhost:8080/medicos?especialidadId=<uuid>"

# 2. Ver slots de un médico
curl "http://localhost:8080/medicos/<medicoId>/slots?fecha=2026-11-24"

# 3. Pedir OTP
curl -X POST http://localhost:8080/auth/paciente/otp \
  -H 'Content-Type: application/json' \
  -d '{"dni":"30111222","apellido":"Paciente","nombre":"Ana","email":"ana@example.com","telefono":"1122334455","consentimiento":true}'

# 4. Verificar OTP y guardar cookies
curl -c cookies.txt -X POST http://localhost:8080/auth/paciente/otp/verificar \
  -H 'Content-Type: application/json' \
  -d '{"dni":"30111222","codigo":"123456"}'

# 5. Reservar
curl -X POST http://localhost:8080/turnos \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer <accessToken>" \
  -d '{"medicoId":"<uuid>","fechaHora":"2026-11-24T09:00:00-03:00","motivo":"Control"}'

# 6. Ver mis turnos
curl http://localhost:8080/turnos/mios -H "Authorization: Bearer <accessToken>"

# 7. Refrescar sesión
curl -b cookies.txt -c cookies.txt -X POST http://localhost:8080/auth/refresh
```
