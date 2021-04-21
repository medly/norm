---
title: Custom Task
has_children: false
nav_order: 2
---

## Using a custom Gradle Task

IF we cannot use the recommended Norm plugin for some reason, but can use gradle, then 
we can add a Gradle task in `build.gradle` which would execute Norm's code generation

```gradle

configurations { norm }

dependencies {
    norm "org.postgresql:postgresql:$postgresVersion" 
    norm "com.medly.norm:runtime:$normVersion"
    norm "com.medly.norm:cli:$normVersion"
} 

task compileNorm(type: JavaExec) {
    classpath = configurations.norm  
    main = "norm.cli.NormCliKt"
    args "${rootProject.rootDir}/sql"                //input dir
    args "${rootProject.rootDir}/gen"                //output dir
    args "jdbc:postgresql://localhost/postgres"      //postgres connection string with db name
    args "postgres"                                  //db username
    args ""                                          //db password (optional for local) 
}
```     

Running `./gradlew compileNorm`  will generate a package structure within `gen` (output dir) with the same path as inside `sql` (input dir).
