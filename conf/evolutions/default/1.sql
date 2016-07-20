# --- !Ups

create table "users" (
    "name"           VARCHAR NOT NULL,
    "documentNumber" VARCHAR NOT NULL,
    "enabled"        BOOLEAN NOT NULL,
    "id"             INT     NOT NULL PRIMARY KEY
);

# --- !Downs

drop table "users";