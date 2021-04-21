---
title: About
has_children: false
nav_order: 1
---

# Norm


![Maven Central](https://img.shields.io/maven-central/v/com.medly.norm/runtime?color=green)
[![Gradle Plugin](https://img.shields.io/maven-metadata/v?color=green&label=Gradle%20Plugin&logo=Gradle&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Fcom.medly.norm%2Fplugin%2Fmaven-metadata.xml)](https://plugins.gradle.org/plugin/com.medly.norm)

NORM is an acronym for Not an ORM. It is purpose-built to leverage the power of SQL with Kotlin. 

SQL is a solid and battle-tested data access language.  While most ORMs and database-access libraries try 
to abstract the SQL away, Norm works on the exact opposite principle. Norm gives SQL back the control it deserves. 

Norm has two major components, a compile time code generator and a very lightweight runtime module (just ~2kb). 

1. Norm's code generator connects to our development database to infer schema and generate the data-access API. 
The generated Kotlin code gives us a type-safe database access layer with zero boilerplate code. 

2. Norm's Runtime provides extension methods to execute the generated code with ease. 


## Norm's Design Choices

- Should allow us to execute arbitrarily complex and performant SQL with ease
- Reduce the boilerplate code to zero by generating all the required code in a separate source set
- Provide a type-safe database access API; whenever the database model changes, we get compilation errors rather than runtime exceptions
- No annotation soup, No bytecode magic. We should be able to see, modify, and check-in all the generated code (if needed)
- Differentiate between Query v/s Commands at the API level. Roughly speaking, queries return results without modifying the database state, whereas commands change the database state but do not return anything.
- Ensure data class properties are immutable by default
