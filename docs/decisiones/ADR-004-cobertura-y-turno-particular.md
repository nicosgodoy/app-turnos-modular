# ADR-004: cobertura aceptada por médico y turno particular

- **Estado**: aceptada.
- **Fecha**: 2026-11-22.

## Contexto

La clínica trabaja con varias obras sociales, pero no todos los médicos aceptan todas. Un paciente puede tener una cobertura que su médico no atiende. No hay pagos online: solo se registra el precio informativo.

## Decisión

- `medico_cobertura` declara qué obra social y plan acepta cada médico. Un registro con `plan_id = null` significa que acepta toda la obra social.
- Al reservar:
  - Si el paciente pide `PARTICULAR`, el turno es particular con `precio = medico.precioParticular`.
  - Si el paciente pide `OBRA_SOCIAL` y el médico no la acepta, se responde `409`.
  - Si no especifica tipo, se usa obra social cuando el médico la acepta; en caso contrario, particular.
- El turno guarda una copia de obra social, plan, número de afiliado y precio para conservar el contexto histórico.

## Consecuencias

- La búsqueda pública puede filtrar médicos por obra social y plan.
- El paciente ve el precio particular antes de confirmar.
- No se calcula copago ni se valida credencial: eso queda fuera del alcance.
- Cambios futuros en el catálogo no alteran turnos ya creados.
