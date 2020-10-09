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

CREATE TABLE projects (
    id serial PRIMARY KEY,
    employee_id INT UNIQUE NOT NULL REFERENCES employees(id),
    name VARCHAR NOT NULL,
    department VARCHAR UNIQUE NOT NULL REFERENCES departments(name)
);
