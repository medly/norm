package norm

import io.kotlintest.matchers.string.shouldContain
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
                generatedFileContent shouldContain "    ps.setArray(1, ps.connection.createArrayOf(\"INT\", params.id))"
                generatedFileContent shouldContain "  }"

                println(generatedFileContent)
            }

        }

    }
}
