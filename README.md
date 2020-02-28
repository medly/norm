## Norm 

### Concept:

**Norm(No-ORM)**, as the name suggests, is a library whose idealogy is opposite to that of how standard ORMs work. Norm is designed to avoid two things:
1. Having to write entity, query and result/response classes by hand 
2. Writing queries/commands which would throw syntax or semantic errors at runtime


### Design:

It is a library for Kotlin and postgres which enables generating query/command classes at compile time if the query is syntactically and semantically right, and provides methods to execute them on runtime. 

Hence, Norm has two packages:

1. ### **codegen**
    Given a postgres sql query or command in the form of a .sql file, this package generates a query class, params class, paramSetter class and resultMapper class in Kotlin. 

    #### How it works:
    
    a. It needs a postgres connection 
    
    b. Analyzes the sql file by creating a [PreparedStatement](https://docs.oracle.com/javase/7/docs/api/java/sql/PreparedStatement.html)
    
    c. If no database errors, creates a [SqlModel](https://github.com/medly/norm/blob/master/codegen/src/main/kotlin/norm/SqlAnalyzer.kt) which acts as an input to CodeGenerator
    
    d. Kotlin file with classes of Query or Command, ParamSetter and RowMapper are generated from SqlModel
     

2. ### **runtime**

    #### What it does:
    
    a. Provides [interfaces](https://github.com/medly/norm/blob/master/runtime/src/main/kotlin/norm/TypedSqlExtensions.kt) which are super types for all query, command, params, row mapper classes.
    
    b. Provides multiple [functions](https://github.com/medly/norm/blob/master/runtime/src/main/kotlin/norm/SqlExtensions.kt) on these interfaces to be able to execute query/command, map result to a list, execute batch commands and queries etc.
     
    These methods run against a [Connection](https://docs.oracle.com/javase/7/docs/api/java/sql/Connection.html) or on a [ResultSet](https://docs.oracle.com/javase/7/docs/api/java/sql/ResultSet.html) or on a [PreparedStatement](https://docs.oracle.com/javase/7/docs/api/java/sql/PreparedStatement.html).

