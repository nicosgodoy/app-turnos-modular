-- El día de la semana se modela como entero (1 = lunes, 7 = domingo).
-- Se amplía de smallint a integer para que coincida con el mapeo JPA.
alter table disponibilidad
    alter column dia_semana type integer;
