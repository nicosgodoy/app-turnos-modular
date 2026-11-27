# App de Turnos — Clínica Sagrada Familia

Aplicación web responsive para la gestión de turnos de una clínica única. Permite a los pacientes buscar médicos por obra social o de forma general, reservar turnos con verificación por email, y a médicos y administradores gestionar agendas y turnos.

## Stack

- **Backend:** Spring Boot 4.1.2, Java 21, Spring Data JPA, Spring Security, Flyway, PostgreSQL.
- **Frontend:** React + Vite + TypeScript, TanStack Query, Tailwind CSS.
- **Infra local:** Docker Compose (PostgreSQL + Mailpit).
- **Documentación:** Markdown + diagramas Mermaid en `docs/`.

## Requisitos

- JDK 21 (en Windows: `C:\Program Files\Java\jdk-21`).
- Docker Desktop iniciado.
- Node.js 22+ (para el frontend).

## Puesta en marcha

```bash
# 1. Base de datos y servidor de correo de desarrollo
docker compose -f infra/docker-compose.yml up -d

# 2. Backend (desde Windows o WSL con cmd.exe)
cd backend-turnoApp
cmd.exe /c "mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev"

# 3. Frontend
cd frontend-turnoApp
npm install
npm run dev
```

- API: <http://localhost:8080>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Mailpit (emails de desarrollo): <http://localhost:8025>
- Frontend: <http://localhost:5173>

> En WSL no hay JDK, por eso se invoca `mvnw.cmd` mediante `cmd.exe`. Ver `AGENTS.md`.

## Usuarios de desarrollo

El perfil `dev` crea datos de demostración al arrancar:

| Rol | Email | Contraseña |
|---|---|---|
| Administrador | `nicodepor3.1@gmail.com` | `admin1234` |
| Médica | `laura.gomez@sagradafamilia.local` | `demo1234` |
| Médico | `martin.perez@sagradafamilia.local` | `demo1234` |

Los pacientes no tienen contraseña: ingresan con DNI + apellido y reciben un código OTP en el email registrado. En desarrollo, el código se ve en Mailpit.

## Variables de entorno

| Variable | Uso |
|---|---|
| `APP_JWT_SECRET` | Firma de tokens (mínimo 32 caracteres). Obligatoria en producción. |
| `APP_ADMIN_EMAIL` / `APP_ADMIN_PASSWORD` | Administrador inicial. |
| `APP_DEMO_PASSWORD` | Contraseña de médicos de demostración. |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Conexión a PostgreSQL en producción. |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | SMTP en producción. |
| `APP_CORS_ORIGINS` | Orígenes permitidos del frontend. |

## Estructura

```
app-turnos-modular/
├── AGENTS.md
├── README.md
├── backend-turnoApp/     # API REST (monolito modular)
├── frontend-turnoApp/    # SPA React
├── infra/                # docker-compose
└── docs/                 # arquitectura, modelo, API, flujos, diagramas
```

## Documentación

- [Arquitectura](docs/arquitectura.md)
- [Modelo de datos](docs/modelo-datos.md)
- [API](docs/api.md)
- [Flujos](docs/flujos.md)
- [Decisiones (ADRs)](docs/decisiones/)
- [Diagramas](docs/diagramas/)
