# ADR-001: monolito modular clásico

- **Estado**: aceptada.
- **Fecha**: 2026-11-22.

## Contexto

La aplicación es una sola clínica con una única sede y un equipo pequeño. Se necesita una base fácil de entender, con límites claros entre dominios, sin la complejidad operativa de microservicios ni la ceremonia de DDD/arquitectura hexagonal.

## Decisión

Organizar el backend como un **monolito modular package-by-feature**. Cada módulo (`usuario`, `paciente`, `cobertura`, `medico`, `agenda`, `turno`, `notificacion`, `panel`, `demo`) contiene sus subpaquetes técnicos `controller`, `service`, `repository`, `entity` y `dto`. `shared` concentra lo transversal.

Reglas:

- Un módulo no usa repositorios de otro; la comunicación va por servicios públicos o por entidades del modelo compartido.
- No hay ciclos entre módulos. Se verifica con ArchUnit.
- Los controladores no acceden a repositorios.

## Consecuencias

- Baja curva de aprendizaje y despliegue simple.
- Los límites son verificables automáticamente.
- Si un módulo crece demasiado, se puede extraer a un servicio sin reescribir el dominio.
- No se obtiene aislamiento de despliegue ni escalado independiente.
