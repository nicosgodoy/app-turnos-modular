# ADR-002: OTP para pacientes y JWT para staff

- **Estado**: aceptada.
- **Fecha**: 2026-11-22.

## Contexto

Los pacientes reservan ocasionalmente y no deberían recordar otra contraseña. El personal de la clínica sí necesita una sesión persistente y segura para operar el sistema durante toda la jornada.

## Decisión

- **Pacientes**: login sin contraseña con DNI + apellido. Se envía un OTP de 6 dígitos por email, válido 10 minutos, con 5 intentos y un máximo de 3 envíos cada 15 minutos. El código se guarda hasheado con BCrypt.
- **Staff**: login con email y contraseña (BCrypt). Se emite un access token JWT de 15 minutos y un refresh token de 7 días en cookie `httpOnly`, `SameSite=Strict`, `path=/auth`.
- El refresh token se guarda hasheado (SHA-256) y rota en cada uso.
- Los pacientes quedan `activo = false` hasta verificar el primer OTP.

## Consecuencias

- Menos fricción para reservar.
- El email se vuelve crítico para el acceso de pacientes; en desarrollo se puede loguear el OTP (`app.otp.log-codigo=true`) y usar Mailpit.
- El refresh token en cookie reduce la exposición del token de larga duración a JavaScript.
- Requiere configurar SMTP confiable en producción.
