-- Catálogos iniciales.

insert into especialidad (nombre, duracion_default_min) values
    ('Clínica Médica', 30),
    ('Pediatría', 30),
    ('Cardiología', 30),
    ('Dermatología', 30),
    ('Ginecología', 30),
    ('Traumatología', 30)
on conflict (nombre) do nothing;

insert into obra_social (nombre, codigo) values
    ('OSDE', 'OSDE'),
    ('Swiss Medical', 'SWISS'),
    ('IOMA', 'IOMA'),
    ('PAMI', 'PAMI')
on conflict (codigo) do nothing;

insert into plan (obra_social_id, nombre, codigo)
select id, 'Plan 210', '210' from obra_social where codigo = 'OSDE'
on conflict (obra_social_id, codigo) do nothing;

insert into plan (obra_social_id, nombre, codigo)
select id, 'Plan 310', '310' from obra_social where codigo = 'OSDE'
on conflict (obra_social_id, codigo) do nothing;

insert into plan (obra_social_id, nombre, codigo)
select id, 'Plan 410', '410' from obra_social where codigo = 'OSDE'
on conflict (obra_social_id, codigo) do nothing;

insert into plan (obra_social_id, nombre, codigo)
select id, 'SMG20', 'SMG20' from obra_social where codigo = 'SWISS'
on conflict (obra_social_id, codigo) do nothing;

insert into plan (obra_social_id, nombre, codigo)
select id, 'SMG30', 'SMG30' from obra_social where codigo = 'SWISS'
on conflict (obra_social_id, codigo) do nothing;

insert into plan (obra_social_id, nombre, codigo)
select id, 'Plan 1000', '1000' from obra_social where codigo = 'IOMA'
on conflict (obra_social_id, codigo) do nothing;

insert into plan (obra_social_id, nombre, codigo)
select id, 'Único', 'UNICO' from obra_social where codigo = 'PAMI'
on conflict (obra_social_id, codigo) do nothing;

insert into consultorio (nombre, ubicacion) values
    ('Consultorio 1', 'Planta baja'),
    ('Consultorio 2', 'Planta baja'),
    ('Consultorio 3', 'Primer piso')
on conflict (nombre) do nothing;
