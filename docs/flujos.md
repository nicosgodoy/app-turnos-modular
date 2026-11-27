# Flujos

## 1. Búsqueda y reserva de turno

```mermaid
sequenceDiagram
    actor P as Paciente
    participant W as Frontend
    participant API as Backend
    participant AG as Agenda
    participant T as Turno
    participant N as Notificación

    P->>W: Elige especialidad u obra social
    W->>API: GET /medicos?especialidadId=...
    API-->>W: Médicos que aceptan la cobertura
    P->>W: Elige médico y fecha
    W->>API: GET /medicos/{id}/slots?fecha=...
    API->>AG: Genera slots
    AG-->>API: Horarios libres
    API-->>W: Slots
    P->>W: Inicia sesión con DNI + OTP
    W->>API: POST /auth/paciente/otp
    API->>N: Envía OTP por email
    W->>API: POST /auth/paciente/otp/verificar
    API-->>W: Access token + cookie de refresh
    P->>W: Completa motivo y confirma
    W->>API: POST /turnos
    API->>AG: Valida que el slot siga libre
    API->>T: Guarda turno CONFIRMADO
    T->>N: Envía email de confirmación
    API-->>W: Turno confirmado
```

Puntos clave:

- El slot se vuelve a validar al reservar: entre la consulta y la confirmación pudo ocuparse.
- El índice único parcial evita la doble reserva aunque dos personas confirmen a la vez.
- Si el médico no acepta la cobertura del paciente, el turno se crea como `PARTICULAR` con el precio del médico.
- La reserva exige al menos 2 horas de anticipación y como máximo 90 días.

## 2. Login de paciente con OTP

```mermaid
flowchart TD
    A["DNI + apellido"] --> B{"¿Existe el paciente?"}
    B -- No --> C["Pedir nombre, email, teléfono y consentimiento"]
    C --> D["Crear usuario PACIENTE inactivo y paciente"]
    B -- Sí --> E{"¿Coincide el apellido?"}
    E -- No --> F["Error: datos no coinciden"]
    E -- Sí --> G{"¿Tiene usuario?"}
    G -- No --> H["Crear usuario PACIENTE y vincular"]
    G -- Sí --> I["Continuar"]
    D --> I
    H --> I
    I --> J{"¿Menos de 3 envíos en 15 min?"}
    J -- No --> K["429: demasiadas solicitudes"]
    J -- Sí --> L["Invalidar OTP anteriores"]
    L --> M["Generar código de 6 dígitos"]
    M --> N["Guardar hash BCrypt con vencimiento 10 min"]
    N --> O["Enviar email"]
    O --> P["Paciente ingresa código"]
    P --> Q{"¿Válido, vigente y con intentos?"}
    Q -- No --> R["Error e incremento de intentos"]
    Q -- Sí --> S["Marcar usado, activar usuario y emitir tokens"]
```

## 3. Cancelación y reprogramación

```mermaid
flowchart TD
    A["Paciente elige un turno"] --> B{"¿Estado activo?"}
    B -- No --> C["409: el turno ya no se puede modificar"]
    B -- Sí --> D{"¿Faltan 24 horas o más?"}
    D -- No --> E["403: solo hasta 24 horas antes"]
    D -- Sí --> F{"¿Cancelar o reprogramar?"}
    F -- Cancelar --> G["Estado CANCELADO_PACIENTE"]
    G --> H["Liberar slot y enviar email"]
    F -- Reprogramar --> I["Validar nuevo slot"]
    I --> J{"¿Sigue libre?"}
    J -- No --> K["409: elegir otro horario"]
    J -- Sí --> L["Actualizar fecha, consultorio y duración"]
    L --> M["Enviar email de reprogramación"]
```

El turno cancelado deja de ocupar el índice único parcial y el horario vuelve a aparecer en `/slots`.

## 4. Sesión de staff

```mermaid
sequenceDiagram
    actor U as Admin/Médico
    participant W as Frontend
    participant API as Backend
    participant DB as PostgreSQL

    U->>W: Email y contraseña
    W->>API: POST /auth/staff/login
    API->>DB: Buscar usuario y verificar BCrypt
    API->>DB: Guardar refresh token hasheado
    API-->>W: Access token (15 min) + cookie refresh (7 días)
    W->>API: GET /admin/turnos con Bearer
    API-->>W: Datos
    Note over W,API: Cuando el access token vence
    W->>API: POST /auth/refresh con cookie
    API->>DB: Verificar hash, revocar anterior y crear nuevo
    API-->>W: Nuevo access token + nueva cookie
```

## 5. Recordatorio 24 horas antes

```mermaid
sequenceDiagram
    participant J as Job diario 09:00
    participant T as TurnoRepository
    participant N as NotificacionService
    participant M as SMTP

    J->>T: Turnos CONFIRMADO/PENDIENTE de mañana
    loop Cada turno
        J->>N: ¿Ya existe recordatorio enviado?
        alt No existe
            N->>M: Enviar email
            N->>N: Guardar notificación ENVIADA o ERROR
        end
    end
```

El job corre con la zona `America/Argentina/Buenos_Aires`. Si el email falla, la notificación queda en `ERROR` y no se reintenta automáticamente en el mismo día.

## 6. Alta de médico y agenda (admin)

```mermaid
flowchart LR
    A["Admin crea especialidades"] --> B["Admin crea consultorios"]
    B --> C["Admin crea médico + usuario"]
    C --> D["Asigna especialidades y coberturas"]
    D --> E["Carga franjas semanales"]
    E --> F["Carga excepciones"]
    F --> G["El médico aparece en la búsqueda pública"]
    G --> H["Se generan slots para los próximos 90 días"]
```

## 7. Reprogramación por parte de la clínica

El admin puede cancelar un turno con `PATCH /admin/turnos/{id}/cancelar?motivo=...`. El turno pasa a `CANCELADO_CLINICA`, se libera el slot y se envía email al paciente. No existe todavía un endpoint para que el admin reprograme directamente: se cancela y el paciente reserva un nuevo horario.
