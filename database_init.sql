create table empleado
(
    id bigint not null primary key,
    nombre varchar(60) not null unique,
    correo varchar(60) not null unique
);

create sequence empleado_id_seq start with 1 increment by 1;
