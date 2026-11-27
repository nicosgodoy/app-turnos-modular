# ADR-006: notificaciones por email y recordatorio diario

- **Estado**: aceptada.
- **Fecha**: 2026-11-22.

## Contexto

El paciente necesita confirmación al reservar, aviso al cancelar o reprogramar, y un recordatorio antes del turno. No se requiere una plataforma de mensajería ni reintentos complejos en esta etapa.

## Decisión

- `NotificacionService` registra cada intento en la tabla `notificacion` con estado `PENDIENTE`, `ENVIADA` o `ERROR`.
- Los emails se envían por SMTP (`spring-boot-starter-mail`). En desarrollo se usa Mailpit.
- El registro se hace en la misma transacción que el turno: si el turno se revierte, la notificación también.
- Un job `@Scheduled` corre todos los días a las 09:00 de `America/Argentina/Buenos_Aires` y envía recordatorios de los turnos del día siguiente.
- Antes de enviar un recordatorio se verifica que no exista uno `ENVIADA` para ese turno, lo que hace el job idempotente.

## Consecuencias

- Trazabilidad de qué se envió y qué falló.
- Un fallo de SMTP no revierte la reserva: la notificación queda en `ERROR`.
- No hay reintentos automáticos ni cola de mensajes; si el volumen crece, se puede agregar un outbox o un proveedor transaccional.
- El horario del job es fijo; si se necesita otra hora, se cambia por configuración en el código.
