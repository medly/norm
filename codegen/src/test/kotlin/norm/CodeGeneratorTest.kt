package norm

import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldNotContain
import io.kotlintest.specs.StringSpec
import org.junit.ClassRule
import org.postgresql.ds.PGSimpleDataSource


class CodeGeneratorTest : StringSpec() {

    @ClassRule
    private val postgreSQLContainer = MyPostgreSQLContainer().withInitScript("init_postgres.sql")

    init {
        postgreSQLContainer.start()
        val dataSource = PGSimpleDataSource().also {
            it.setUrl(postgreSQLContainer.jdbcUrl)
            it.user = postgreSQLContainer.username
            it.password = postgreSQLContainer.password
        }

        "Query class generator" {
            dataSource.connection.use {
                val generatedFileContent = codegen(it, "select * from employees where first_name = :name order by :field", "com.foo", "Foo")

                generatedFileContent shouldContain "data class FooResult("
                generatedFileContent shouldContain "data class FooParams("
                generatedFileContent shouldContain "class FooParamSetter : ParamSetter<FooParams> {"
                generatedFileContent shouldContain "class FooRowMapper : RowMapper<FooResult> {"
                generatedFileContent shouldContain "class FooQuery : Query<FooParams, FooResult> {"

                println(generatedFileContent)
            }
        }

        "Command class generator"{
            dataSource.connection.use {
                val generatedFileContent = codegen(it, "insert into employees (first_name,last_name) values (:firstName, :lastName)", "com.foo", "Foo")
                generatedFileContent shouldContain "data class FooParams("
                generatedFileContent shouldContain "class FooParamSetter : ParamSetter<FooParams> {"
                generatedFileContent shouldContain "class FooCommand : Command<FooParams> {"

                println(generatedFileContent)
            }
        }

        "Should generate query parameter with Array type while using ANY operator"{

            dataSource.connection.use {
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

            dataSource.connection.use {
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

            dataSource.connection.use {
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

        "should generate empty params class if inputs params are not present" {
            dataSource.connection.use {
                val generatedFileContent = codegen(it, "select * from  employees", "com.foo", "Foo")
                generatedFileContent shouldNotContain  "data class FooParams"
                generatedFileContent shouldContain "class FooParams"

                println(generatedFileContent)
            }
        }
    }
}
