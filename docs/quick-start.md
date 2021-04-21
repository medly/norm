---
title: Quick Start
has_children: false
nav_order: 2
---

## Quick Start

1. Add Norm's Gradle plugin to your data-access project in case of a multi-module project, or to the root project

    ```gradle
    plugins {
        id 'com.medly.norm' version '0.0.6'
    }
    ```


2. Configure the plugin. 

    ```groovy
    norm {
        jdbcUrl = "jdbc:postgresql://localhost/postgres"
        username = "postgres"
        password = ""
        inputDir = file("src/main/sql")
        outDir = file("src/main/gen")
        basePath = file("src/main/gen")
    }
    ```

    The database `jdbcUrl`, `username`, and `password` here refer to local PostgreSQL instance. Also it is highly recommended to use a schema migration utility like Liquibase or Flyway to manage schema versioning. 



3. Create a sql file in `inputDir` under a directory path which should reflect our package name e.g `com/foo/person-by-age.sql`

    ```sql
    SELECT * FROM persons WHERE AGE > :age;
    ```


4. Generate the kotlin code using the gradle task

    ```shell
    $ ./gradlew normCodegen
    ```

5. Access the database. we should inject the `datasource` object using IoC framework like Spring or Micronaut.

    ```kotlin
      datasource.connection.use { connection ->
          FindPersonQuery().query(connection, FindPersonParams("28"))
      }.forEach {
          println(it.toString())
      }
    ```

