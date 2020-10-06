CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    first_name varchar,
    last_name varchar
);

CREATE TABLE departments (
    id SERIAL PRIMARY KEY,
    name varchar
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

CREATE TABLE genre(
genre_id INT PRIMARY KEY,
title VARCHAR NOT NULL,
description VARCHAR NOT NULL
);

CREATE TABLE movie(
id serial PRIMARY KEY,
name varchar,
genre_id INT NOT NULL REFERENCES genre(genre_id));
