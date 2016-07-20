# --- !Ups

create table "users" (
    "name"           VARCHAR NOT NULL,
    "documentNumber" VARCHAR NOT NULL,
    "enabled"        BOOLEAN NOT NULL,
    "id"             INT     NOT NULL PRIMARY KEY AUTO_INCREMENT
);

# --- !Downs

drop table "users";