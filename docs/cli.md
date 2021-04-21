---
title: CLI
has_children: false
nav_order: 3
---

## Command Line Interface

Norm CLI can be used to generate Kotlin files corresponding to SQL files. This is handy in case Gradle (or Norm Gralde plugin) cannot be used.

We can provide multiple of files using  `-f some/path/a.sql -f some/path/b.sql`. This will generate Kotlin files
at `some.path.A.kt` & `some.path.B.kt`. If we want to exclude `some` from the package name then we must use `-b` option 
with the base dir `-f some/path/a.sql -f some/path/b.sql -b some/`. Now the kotlin files will be generated in package
`path.A.kt` & `path.A.kt` inside the `output-dir`.

If option `--in-dir` is used, all the `*.sql` files will be used for code generation.

```terminal
$ norm-codegen --help

Usage: norm-codegen [OPTIONS] [SQLFILES]...

  Generates Kotlin Source files for given SQL files using the Postgres
  database connection

Options:
  -j, --jdbc-url TEXT        JDBC connection URL (can use env var PG_JDBC_URL)
  -u, --username TEXT        Username (can use env var PG_USERNAME)
  -p, --password TEXT        Password (can use env var PG_PASSWORD)
  -b, --base-path DIRECTORY  relative path from this dir will be used to infer
                             package name
  -f, --file FILE            [Multiple] SQL files, the file path relative to
                             base path (-b) will be used to infer package name
  -d, --in-dir DIRECTORY     Dir containing .sql files, relative path from
                             this dir will be used to infer package name
  -o, --out-dir DIRECTORY    Output dir where source should be generated
  -h, --help                 Show this message and exit

```
