# ADR-005: anti doble reserva con índice único parcial

- **Estado**: aceptada.
- **Fecha**: 2026-11-22.

## Contexto

Dos pacientes podrían confirmar el mismo horario casi al mismo tiempo. Validar solo en la aplicación deja una ventana de carrera entre la consulta y el `insert`.

## Decisión

Crear un índice único parcial en PostgreSQL:

```sql
create unique index ux_turno_medico_fecha
    on turno (medico_id, fecha_hora)
    where estado not in ('CANCELADO_PACIENTE', 'CANCELADO_CLINICA');
```

La aplicación valida el slot antes de guardar, pero la garantía final es el índice. Si el `insert` viola la restricción, `TurnoService` captura `DataIntegrityViolationException` y responde `409` con un mensaje accionable.

## Consecuencias

- Imposible duplicar un turno activo para el mismo médico y horario, incluso con concurrencia.
- Un turno cancelado libera el horario automáticamente.
- `ATENDIDO` y `AUSENTE` siguen ocupando el horario, como corresponde al historial.
- La regla vive en la base y no depende de que todos los caminos de código la recuerden.
