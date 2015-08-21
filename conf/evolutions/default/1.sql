# Tasks schema

# --- !Ups

CREATE TABLE Person (
  id BIGSERIAL PRIMARY KEY,
  name TEXT,
  documentNumber TEXT,
  enabled BOOLEAN
);

# --- !Downs

DROP TABLE Person;
