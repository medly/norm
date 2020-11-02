package norm.codegen

import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.testcontainers.perSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import norm.test.utils.PgContainer
import norm.test.utils.codegen
import norm.util.withPgConnection


class CodeGeneratorTest : StringSpec() {

    private val pgContainer: PgContainer = PgContainer().withInitScript("init_postgres.sql")

    override fun listeners() = listOf(pgContainer.perSpec())

    init {

        "Query class generator" {
            withPgConnection(pgContainer.jdbcUrl, pgContainer.username, pgContainer.password) {
                val generatedFileContent = codegen(it, "select * from employees where first_name = :name order by :field", "com.foo", "Foo").trimIndent()
                generatedFileContent shouldContain """
data class FooParams(
  val name: String?,
  val field: String?
)

class FooParamSetter : ParamSetter<FooParams> {
  override fun map(ps: PreparedStatement, params: FooParams) {
    ps.setObject(1, params.name)
    ps.setObject(2, params.field)
  }
}

data class FooResult(
  val id: Int,
  val firstName: String?,
  val lastName: String?
)

class FooRowMapper : RowMapper<FooResult> {
  override fun map(rs: ResultSet): FooResult = FooResult(
  id = rs.getObject("id") as kotlin.Int,
    firstName = rs.getObject("first_name") as kotlin.String?,
    lastName = rs.getObject("last_name") as kotlin.String?)
}

class FooQuery : Query<FooParams, FooResult> {
  override val sql: String = "select * from employees where first_name = ? order by ?"

  override val mapper: RowMapper<FooResult> = FooRowMapper()

  override val paramSetter: ParamSetter<FooParams> = FooParamSetter()
}
                """.trimIndent()


            }
        }

        "Command class generator"{
            withPgConnection(pgContainer.jdbcUrl, pgContainer.username, pgContainer.password) {
                val generatedFileContent = codegen(it, "insert into employees (first_name,last_name) values (:firstName, :lastName)", "com.foo", "Foo")
                generatedFileContent shouldContain "data class FooParams("
                generatedFileContent shouldContain "class FooParamSetter : ParamSetter<FooParams> {"
                generatedFileContent shouldContain "class FooCommand : Command<FooParams> {"

                println(generatedFileContent)
            }
        }

        "Should generate query parameter with Array type while using ANY operator"{

            withPgConnection(pgContainer.jdbcUrl, pgContainer.username, pgContainer.password) {
                val generatedFileContent = codegen(it, "select * from  employees where id = ANY(:id) ", "com.foo", "Foo")
                generatedFileContent shouldContain "data class FooParams("
                generatedFileContent shouldContain "  val id: Array<Int>?"

                generatedFileContent shouldContain "class FooParamSetter : ParamSetter<FooParams> {"
                generatedFileContent shouldContain "  override fun map(ps: PreparedStatement, params: FooParams) {"
                generatedFileContent shouldContain "    ps.setArray(1, ps.connection.createArrayOf(\"int4\", params.id))"
                generatedFileContent shouldContain "  }"

                println(generatedFileContent)
            }

        }
        "should accept array as parameter while searching inside array using @> contains operator"{

            withPgConnection(pgContainer.jdbcUrl, pgContainer.username, pgContainer.password) {
                val generatedFileContent = codegen(it, "SELECT * FROM combinations WHERE colors  @> :colors ;", "com.foo", "Foo")
                generatedFileContent shouldContain "data class FooParams("
                generatedFileContent shouldContain "  val colors: Array<String>?"

                generatedFileContent shouldContain "class FooParamSetter : ParamSetter<FooParams> {"
                generatedFileContent shouldContain "  override fun map(ps: PreparedStatement, params: FooParams) {"
                generatedFileContent shouldContain "    ps.setArray(1, ps.connection.createArrayOf(\"varchar\", params.colors))"
                generatedFileContent shouldContain "  }"

                println(generatedFileContent)
            }

        }

        "should support jsonb type along with array"{

            withPgConnection(pgContainer.jdbcUrl, pgContainer.username, pgContainer.password) {
                val generatedFileContent = codegen(it, "insert into owners(colors,details) VALUES(:colors,:details)", "com.foo", "Foo")
                generatedFileContent shouldContain "data class FooParams("
                generatedFileContent shouldContain "  val colors: Array<String>?"
                generatedFileContent shouldContain "  val details: PGobject?"

                generatedFileContent shouldContain "class FooParamSetter : ParamSetter<FooParams> {"
                generatedFileContent shouldContain "  override fun map(ps: PreparedStatement, params: FooParams) {"
                generatedFileContent shouldContain "    ps.setArray(1, ps.connection.createArrayOf(\"varchar\", params.colors))"
                generatedFileContent shouldContain "    ps.setObject(2, params.details)"
                generatedFileContent shouldContain "  }"

                println(generatedFileContent)
            }
        }

        "should support custom java8 time data type"{

            withPgConnection(pgContainer.jdbcUrl, pgContainer.username, pgContainer.password) {
                val generatedFileContent = codegen(it, "insert into time_travel_log(from_time,to_time,duration) VALUES(:fromTime,:toTime,:duration)", "com.foo", "Foo")
                generatedFileContent shouldContain "data class FooParams("
                generatedFileContent shouldContain "  val fromTime: OffsetDateTime?"
                generatedFileContent shouldContain "  val toTime: OffsetDateTime?"
                generatedFileContent shouldContain "  val duration: LocalTime?"

                generatedFileContent shouldContain "class FooParamSetter : ParamSetter<FooParams> {"
                generatedFileContent shouldContain "  override fun map(ps: PreparedStatement, params: FooParams) {"
                generatedFileContent shouldContain "    ps.setObject(1, params.fromTime)"
                generatedFileContent shouldContain "    ps.setObject(2, params.toTime)"
                generatedFileContent shouldContain "    ps.setObject(3, params.duration)"
                generatedFileContent shouldContain "  }"

                println(generatedFileContent)
            }
        }

        "should correctly map array columns"{

            withPgConnection(pgContainer.jdbcUrl, pgContainer.username, pgContainer.password) {
                val generatedFileContent = codegen(it, "select * from owners", "com.foo", "Foo")
                generatedFileContent shouldContain "class FooRowMapper : RowMapper<FooResult> {\n" +
                        "  override fun map(rs: ResultSet): FooResult = FooResult(\n" +
                        "  id = rs.getObject(\"id\") as kotlin.Int,\n" +
                        "    colors = rs.getArray(\"colors\").array as kotlin.Array<kotlin.String>?,\n" +
                        "    details = rs.getObject(\"details\") as org.postgresql.util.PGobject?)\n" +
                        "}"
                println(generatedFileContent)
            }
        }

        "should generate empty params class if inputs params are not present" {
            withPgConnection(pgContainer.jdbcUrl, pgContainer.username, pgContainer.password) {
                val generatedFileContent = codegen(it, "select * from  employees", "com.foo", "Foo")
                generatedFileContent shouldNotContain "data class FooParams"
                generatedFileContent shouldContain "class FooParams"

                println(generatedFileContent)
            }
        }
    }
}
