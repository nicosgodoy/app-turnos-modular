-- Esquema inicial de la aplicación de turnos.

create table obra_social (
    id uuid primary key default gen_random_uuid(),
    nombre varchar(120) not null,
    codigo varchar(40) not null,
    activo boolean not null default true,
    creado_en timestamptz not null default now(),
    actualizado_en timestamptz not null default now(),
    constraint ux_obra_social_codigo unique (codigo)
);

create table plan (
    id uuid primary key default gen_random_uuid(),
    obra_social_id uuid not null references obra_social (id),
    nombre varchar(120) not null,
    codigo varchar(40) not null,
    activo boolean not null default true,
    creado_en timestamptz not null default now(),
    actualizado_en timestamptz not null default now(),
    constraint ux_plan_obra_social_codigo unique (obra_social_id, codigo)
);

create table especialidad (
    id uuid primary key default gen_random_uuid(),
    nombre varchar(120) not null,
    duracion_default_min integer not null default 30,
    activo boolean not null default true,
    creado_en timestamptz not null default now(),
    actualizado_en timestamptz not null default now(),
    constraint ux_especialidad_nombre unique (nombre)
);

create table consultorio (
    id uuid primary key default gen_random_uuid(),
    nombre varchar(120) not null,
    ubicacion varchar(200),
    activo boolean not null default true,
    creado_en timestamptz not null default now(),
    actualizado_en timestamptz not null default now(),
    constraint ux_consultorio_nombre unique (nombre)
);

create table usuario (
    id uuid primary key default gen_random_uuid(),
    email varchar(255) not null,
    password_hash varchar(100),
    rol varchar(20) not null,
    nombre varchar(120) not null,
    apellido varchar(120) not null,
    activo boolean not null default true,
    creado_en timestamptz not null default now(),
    actualizado_en timestamptz not null default now(),
    constraint ck_usuario_rol check (rol in ('ADMIN', 'MEDICO', 'PACIENTE'))
);

create unique index ux_usuario_email on usuario (lower(email));

create table paciente (
    id uuid primary key default gen_random_uuid(),
    usuario_id uuid unique references usuario (id) on delete set null,
    nombre varchar(120) not null,
    apellido varchar(120) not null,
    dni varchar(15) not null,
    fecha_nacimiento date,
    telefono varchar(30) not null,
    email varchar(255) not null,
    obra_social_id uuid references obra_social (id),
    plan_id uuid references plan (id),
    numero_afiliado varchar(50),
    consentimiento_datos boolean not null default false,
    creado_en timestamptz not null default now(),
    actualizado_en timestamptz not null default now(),
    constraint ux_paciente_dni unique (dni)
);

create index ix_paciente_apellido on paciente (lower(apellido));

create table medico (
    id uuid primary key default gen_random_uuid(),
    usuario_id uuid unique not null references usuario (id),
    nombre varchar(120) not null,
    apellido varchar(120) not null,
    matricula varchar(40) not null,
    duracion_turno_min integer not null default 30,
    precio_particular numeric(10, 2) not null default 0,
    biografia text,
    activo boolean not null default true,
    creado_en timestamptz not null default now(),
    actualizado_en timestamptz not null default now(),
    constraint ux_medico_matricula unique (matricula)
);

create table medico_especialidad (
    medico_id uuid not null references medico (id) on delete cascade,
    especialidad_id uuid not null references especialidad (id),
    primary key (medico_id, especialidad_id)
);

create table medico_cobertura (
    id uuid primary key default gen_random_uuid(),
    medico_id uuid not null references medico (id) on delete cascade,
    obra_social_id uuid not null references obra_social (id),
    plan_id uuid references plan (id),
    creado_en timestamptz not null default now()
);

create unique index ux_medico_cobertura_sin_plan
    on medico_cobertura (medico_id, obra_social_id)
    where plan_id is null;

create unique index ux_medico_cobertura_con_plan
    on medico_cobertura (medico_id, plan_id)
    where plan_id is not null;

create table disponibilidad (
    id uuid primary key default gen_random_uuid(),
    medico_id uuid not null references medico (id) on delete cascade,
    consultorio_id uuid not null references consultorio (id),
    dia_semana smallint not null,
    hora_desde time not null,
    hora_hasta time not null,
    vigente_desde date not null,
    vigente_hasta date,
    activo boolean not null default true,
    creado_en timestamptz not null default now(),
    actualizado_en timestamptz not null default now(),
    constraint ck_disponibilidad_dia check (dia_semana between 1 and 7),
    constraint ck_disponibilidad_horas check (hora_desde < hora_hasta)
);

create index ix_disponibilidad_medico on disponibilidad (medico_id, dia_semana);

create table excepcion_agenda (
    id uuid primary key default gen_random_uuid(),
    medico_id uuid not null references medico (id) on delete cascade,
    fecha_desde date not null,
    fecha_hasta date not null,
    todo_el_dia boolean not null default true,
    hora_desde time,
    hora_hasta time,
    motivo varchar(255),
    creado_en timestamptz not null default now(),
    actualizado_en timestamptz not null default now(),
    constraint ck_excepcion_fechas check (fecha_desde <= fecha_hasta),
    constraint ck_excepcion_horas check (
        todo_el_dia
        or (hora_desde is not null and hora_hasta is not null and hora_desde < hora_hasta)
    )
);

create index ix_excepcion_medico_fechas on excepcion_agenda (medico_id, fecha_desde, fecha_hasta);

create table turno (
    id uuid primary key default gen_random_uuid(),
    medico_id uuid not null references medico (id),
    paciente_id uuid not null references paciente (id),
    consultorio_id uuid not null references consultorio (id),
    fecha_hora timestamptz not null,
    duracion_min integer not null,
    estado varchar(30) not null,
    tipo_cobertura varchar(20) not null,
    obra_social_id uuid references obra_social (id),
    plan_id uuid references plan (id),
    numero_afiliado varchar(50),
    precio numeric(10, 2),
    motivo varchar(500),
    canal varchar(20) not null default 'WEB',
    cancelado_motivo varchar(500),
    creado_en timestamptz not null default now(),
    actualizado_en timestamptz not null default now(),
    constraint ck_turno_estado check (estado in (
        'PENDIENTE', 'CONFIRMADO', 'ATENDIDO',
        'CANCELADO_PACIENTE', 'CANCELADO_CLINICA', 'AUSENTE'
    )),
    constraint ck_turno_cobertura check (tipo_cobertura in ('OBRA_SOCIAL', 'PARTICULAR'))
);

create unique index ux_turno_medico_fecha
    on turno (medico_id, fecha_hora)
    where estado not in ('CANCELADO_PACIENTE', 'CANCELADO_CLINICA');

create index ix_turno_paciente on turno (paciente_id, fecha_hora desc);
create index ix_turno_medico on turno (medico_id, fecha_hora);
create index ix_turno_fecha on turno (fecha_hora);

create table refresh_token (
    id uuid primary key default gen_random_uuid(),
    usuario_id uuid not null references usuario (id) on delete cascade,
    token_hash varchar(64) not null,
    expira_en timestamptz not null,
    revocado boolean not null default false,
    creado_en timestamptz not null default now(),
    constraint ux_refresh_token_hash unique (token_hash)
);

create index ix_refresh_token_usuario on refresh_token (usuario_id);

create table codigo_otp (
    id uuid primary key default gen_random_uuid(),
    usuario_id uuid not null references usuario (id) on delete cascade,
    codigo_hash varchar(100) not null,
    expira_en timestamptz not null,
    usado boolean not null default false,
    intentos integer not null default 0,
    creado_en timestamptz not null default now()
);

create index ix_codigo_otp_usuario on codigo_otp (usuario_id, creado_en desc);

create table notificacion (
    id uuid primary key default gen_random_uuid(),
    turno_id uuid not null references turno (id) on delete cascade,
    tipo varchar(30) not null,
    destinatario varchar(255) not null,
    estado varchar(20) not null,
    enviado_en timestamptz,
    error text,
    creado_en timestamptz not null default now(),
    constraint ck_notificacion_tipo check (tipo in (
        'CONFIRMACION', 'RECORDATORIO', 'CANCELACION', 'REPROGRAMACION'
    )),
    constraint ck_notificacion_estado check (estado in ('PENDIENTE', 'ENVIADA', 'ERROR'))
);

create index ix_notificacion_turno on notificacion (turno_id, tipo);
