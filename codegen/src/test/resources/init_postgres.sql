CREATE TABLE employees (
  id SERIAL PRIMARY KEY,
  first_name varchar,
  last_name varchar,
  date_of_birth date
);

CREATE TABLE departments (
  id SERIAL PRIMARY KEY,
  name varchar
);

CREATE TABLE combinations(
id serial PRIMARY KEY,
colors varchar[]);

CREATE TABLE owners(
id serial PRIMARY KEY,
colors varchar[],
details jsonb
)

