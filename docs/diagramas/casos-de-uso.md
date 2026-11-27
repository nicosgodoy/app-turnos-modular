# Casos de uso

```mermaid
flowchart LR
    P((Paciente))
    M((Médico))
    A((Administrador))

    subgraph Público
        CU1[Buscar médicos por especialidad u obra social]
        CU2[Ver perfil y disponibilidad del médico]
        CU3[Consultar slots libres]
    end

    subgraph Paciente
        CU4[Ingresar con DNI, apellido y OTP]
        CU5[Reservar turno]
        CU6[Cancelar turno]
        CU7[Reprogramar turno]
        CU8[Ver mis turnos]
        CU9[Actualizar mis datos y cobertura]
    end

    subgraph Médico
        CU10[Ver mi perfil profesional]
        CU11[Consultar mi agenda]
        CU12[Ver turnos del día]
    end

    subgraph Administración
        CU13[Gestionar especialidades]
        CU14[Gestionar obras sociales y planes]
        CU15[Gestionar médicos y coberturas]
        CU16[Gestionar consultorios]
        CU17[Gestionar disponibilidad y excepciones]
        CU18[Gestionar turnos]
        CU19[Gestionar pacientes]
    end

    P --> CU1
    P --> CU2
    P --> CU3
    P --> CU4
    P --> CU5
    P --> CU6
    P --> CU7
    P --> CU8
    P --> CU9

    M --> CU10
    M --> CU11
    M --> CU12

    A --> CU13
    A --> CU14
    A --> CU15
    A --> CU16
    A --> CU17
    A --> CU18
    A --> CU19

    CU5 -.->|incluye| CU4
    CU6 -.->|requiere| CU4
    CU7 -.->|requiere| CU4
    CU8 -.->|requiere| CU4
    CU9 -.->|requiere| CU4
    CU5 -.->|valida| CU3
```

## Detalle de casos principales

### CU5 — Reservar turno

- **Actor**: paciente autenticado.
- **Precondiciones**: sesión con OTP verificada; el médico está activo; el horario está dentro de los próximos 90 días y con 2 horas de anticipación.
- **Flujo principal**: elige médico y slot, completa el motivo, confirma. El sistema valida el slot, determina cobertura, guarda el turno y envía email.
- **Alternativas**: el slot se ocupó (409); el médico no acepta la cobertura y el paciente pidió obra social (409); no hay sesión (401).

### CU6 — Cancelar turno

- **Actor**: paciente autenticado.
- **Precondiciones**: el turno pertenece al paciente, está activo y faltan 24 horas o más.
- **Resultado**: estado `CANCELADO_PACIENTE`, slot liberado, email de cancelación.

### CU7 — Reprogramar turno

- **Actor**: paciente autenticado.
- **Precondiciones**: las mismas que cancelar.
- **Resultado**: nuevo `fechaHora`, consultorio y duración; estado `CONFIRMADO`; email de reprogramación.

### CU18 — Gestionar turnos

- **Actor**: administrador.
- **Acciones**: listar con filtros por fecha, médico y estado; marcar `ATENDIDO` o `AUSENTE`; cancelar desde la clínica con motivo.
- **Resultado**: el paciente recibe email cuando la clínica cancela.
