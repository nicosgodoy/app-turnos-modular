# AGENTS.md

Guía para agentes que trabajen en este repositorio. Todo lo que se agregue acá debe estar verificado contra el código.

## Idioma

- Documentación, diagramas, mensajes de error de cara al usuario y textos de la UI en español.
- Código (Java, TypeScript), tablas y endpoints en inglés.

## Estructura

- Monorepo: `backend-turnoApp/` (Spring Boot), `frontend-turnoApp/` (React + Vite + TS), `infra/`, `docs/`.
- La raíz git es este directorio; `backend-turnoApp/` es un único módulo Maven.
- Paquete base `com.nicosgodoy.backend_turnoApp` (el guion bajo es intencional, ver `backend-turnoApp/HELP.md`).

## Backend

- Spring Boot 4.1.2, Java 21.
- Monolito modular *package-by-feature*, sin DDD ni arquitectura hexagonal. Módulos: `shared`, `usuario`, `paciente`, `medico`, `cobertura`, `agenda`, `turno`, `notificacion`.
- Cada módulo usa subpaquetes técnicos clásicos: `controller`, `service`, `repository`, `entity`, `dto`.
- Regla de módulos: un módulo no usa repositorios de otro; la comunicación va por los servicios públicos del otro módulo (o por sus entidades cuando el dato es parte del modelo compartido).
- Migraciones Flyway en `src/main/resources/db/migration`. Nunca editar una migración ya aplicada: agregar una nueva.
- Config por perfiles (`application.properties`, `application-dev.properties`, `application-prod.properties`). Secretos por variables de entorno (`APP_JWT_SECRET`, `APP_ADMIN_PASSWORD`, `DB_*`, `MAIL_*`).
- Zona horaria de negocio: `America/Argentina/Buenos_Aires`. Persistencia en `timestamptz`.

## Comandos (desde WSL)

- No hay JDK dentro de WSL: `./mvnw` falla con `JAVA_HOME is not defined correctly`. Usar el wrapper de Windows:
  - Suite completa: `cmd.exe /c "mvnw.cmd -B test"`
  - Un test: `cmd.exe /c "mvnw.cmd -B -Dtest=SlotServiceTest test"`
  - App en dev: `cmd.exe /c "mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev"`
- Frontend (Node de Windows): `cd frontend-turnoApp && npm run dev`.
- Infra: `docker compose -f infra/docker-compose.yml up -d` (requiere Docker Desktop iniciado).
- Swagger UI: <http://localhost:8080/swagger-ui.html>. Mailpit: <http://localhost:8025>.

## Pruebas

- Los tests unitarios deben pasar sin Docker.
- Los de integración usan Testcontainers con PostgreSQL y `@Testcontainers(disabledWithoutDocker = true)`; se omiten si el daemon no está disponible.
- No usar H2: el esquema depende de PostgreSQL (índices únicos parciales, `timestamptz`).

## Reglas de negocio clave

- Una sola clínica, sin tenant. Una sede con varios consultorios.
- Turnos por médico con franjas semanales + duración; sin doble reserva (índice único parcial en `turno`).
- Cada médico declara qué obras sociales/planes acepta; si no acepta la cobertura del paciente, el turno es particular con el precio del médico.
- Reserva hasta 90 días adelante y con 2 h de anticipación mínima; cancelar/reprogramar hasta 24 h antes.
- OTP de 6 dígitos: 10 minutos, 3 envíos cada 15 minutos, 5 intentos.
- Roles: `ADMIN`, `MEDICO`, `PACIENTE`.

## Documentación

- `docs/arquitectura.md`, `docs/modelo-datos.md`, `docs/api.md`, `docs/flujos.md`, `docs/decisiones/` (ADRs) y `docs/diagramas/`.
- Diagramas en Mermaid embebidos en Markdown.
- Al cambiar el modelo de datos o los endpoints, actualizar docs y diagramas en el mismo cambio.
