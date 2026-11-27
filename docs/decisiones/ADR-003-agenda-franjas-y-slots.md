# ADR-003: agenda por franjas semanales y slots generados

- **Estado**: aceptada.
- **Fecha**: 2026-11-22.

## Contexto

Cada médico trabaja distintos días, horarios y consultorios. Cargar turnos individuales de antemano sería tedioso y propenso a errores. Además, los médicos necesitan bloquear licencias o feriados sin borrar su agenda habitual.

## Decisión

Modelar la agenda como:

- `disponibilidad`: franja semanal recurrente (`dia_semana`, `hora_desde`, `hora_hasta`, vigencia, consultorio, activo).
- `excepcion_agenda`: rango de fechas, día completo o rango horario parcial, con motivo.
- `SlotService`: genera slots en memoria a partir de la duración del médico, descarta ocupados, excepciones y horarios con menos de 2 horas de anticipación.
- La reserva vuelve a validar el slot antes de persistir.

## Consecuencias

- La agenda se carga una sola vez y se mantiene sola.
- Los slots no se persisten: siempre reflejan la disponibilidad y los turnos actuales.
- Cambiar la duración del médico afecta solo a slots futuros; los turnos ya reservados conservan su `duracionMin`.
- La generación es acotada a 90 días, por lo que el costo de cálculo es bajo.
