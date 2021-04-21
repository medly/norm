---
title: How norm works
has_children: false
nav_order: 4
---

## How Norm works

Norm requires us to have an active database connection to precompile queries for typesafe data access. Codegen module uses this connection to generate the code.
    
#### Codegen
- Norm opens a connection to development database. 
- It reads the SQL file and analyzes query by creating a [PreparedStatement](https://docs.oracle.com/javase/7/docs/api/java/sql/PreparedStatement.html)
- If query is syntactically correct, it creates a [SqlModel](https://github.com/medly/norm/blob/master/codegen/src/main/kotlin/norm/SqlAnalyzer.kt) which is an intermidiary data model that acts as an input to CodeGenerator
- Finally, generates Kotlin files with classes of Query or Command, ParamSetter and RowMapper

#### Runtime     

- Provides generic extension functions and interfaces which can be used without code-gen. These simplify usage of [Connection](https://docs.oracle.com/javase/7/docs/api/java/sql/Connection.html) or [ResultSet](https://docs.oracle.com/javase/7/docs/api/java/sql/ResultSet.html) or [PreparedStatement](https://docs.oracle.com/javase/7/docs/api/java/sql/PreparedStatement.html) classes.
- Adds extentions on these interfaces to be able to execute query/command, map result to a list, execute batch commands and queries etc.



## Peek at the generated code

The content of generated file would look like:
```kotlin 
package person

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Int
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAllPersonsAboveGivenAgeParams(
  val age: Int?
)

class GetAllPersonsAboveGivenAgeParamSetter : ParamSetter<GetAllPersonsAboveGivenAgeParams> {
  override fun map(ps: PreparedStatement, params: GetAllPersonsAboveGivenAgeParams) {
    ps.setObject(1, params.age)
  }
}

data class GetAllPersonsAboveGivenAgeResult(
  val id: Int,
  val name: String?,
  val age: Int?,
  val occupation: String?,
  val address: String?
)

class GetAllPersonsAboveGivenAgeRowMapper : RowMapper<GetAllPersonsAboveGivenAgeResult> {
  override fun map(rs: ResultSet): GetAllPersonsAboveGivenAgeResult =
      GetAllPersonsAboveGivenAgeResult(
  id = rs.getObject("id") as kotlin.Int,
    name = rs.getObject("name") as kotlin.String?,
    age = rs.getObject("age") as kotlin.Int?,
    occupation = rs.getObject("occupation") as kotlin.String?,
    address = rs.getObject("address") as kotlin.String?)
}

class GetAllPersonsAboveGivenAgeQuery : Query<GetAllPersonsAboveGivenAgeParams,
    GetAllPersonsAboveGivenAgeResult> {
  override val sql: String = """
      |SELECT * FROM persons WHERE AGE > ?;
      |""".trimMargin()

  override val mapper: RowMapper<GetAllPersonsAboveGivenAgeResult> =
      GetAllPersonsAboveGivenAgeRowMapper()

  override val paramSetter: ParamSetter<GetAllPersonsAboveGivenAgeParams> =
      GetAllPersonsAboveGivenAgeParamSetter()
}
```

## Using the generated code

To run any query/command, a DataSource connection of postgres is required.

Create an instance of DataSource using the postgresql driver(already added in dependency) methods
```kotlin
  val dataSource = PGSimpleDataSource().also {
          it.setUrl("jdbc:postgresql://localhost/postgres")
          it.user = "postgres"
          it.password = ""
  }
``` 

Finally we can execute the query

```kotlin
  val result = dataSource.connection.use { connection -> 
      GetAllPersonsAboveGivenAgeQuery().query(connection, GetAllPersonsAboveGivenAgeParams(20))
  }
  result.forEach { println(it.toString()) }
```  

And Have fun :)

