CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    first_name varchar,
    last_name varchar
);

CREATE TABLE departments (
    id SERIAL PRIMARY KEY,
    name VARCHAR UNIQUE
);

CREATE TABLE combinations(
    id serial PRIMARY KEY,
    colors varchar[]
);

CREATE TABLE owners(
    id serial PRIMARY KEY,
    colors varchar[],
    details jsonb
);

CREATE TABLE time_travel_log(
    id serial PRIMARY KEY,
    from_time timestamptz,
    to_time timestamptz,
    duration time
);

CREATE TABLE logs(
  id uuid PRIMARY KEY,
  column_name VARCHAR,
  old_value VARCHAR,
  new_value VARCHAR,
  captured_at TIMESTAMPTZ
);

CREATE TABLE requests
(
  id         serial PRIMARY KEY,
  documentId int8,
  type       varchar,
  status     varchar,
  requested_at TIMESTAMPTZ
);
