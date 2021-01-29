package norm.codegen

import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.testcontainers.perSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import norm.test.utils.PgContainer
import norm.test.utils.codegen
import norm.test.utils.readAsResource
import org.postgresql.ds.PGSimpleDataSource
import java.sql.Connection

class CodeGeneratorTest : StringSpec() {

    private val pgContainer: PgContainer = PgContainer().withInitScript("init_postgres.sql")

    override fun listeners() = listOf(pgContainer.perSpec())

    lateinit var connection: Connection

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)

        connection = PGSimpleDataSource().also {
            it.setUrl(pgContainer.jdbcUrl) // url is not a property
            it.user = pgContainer.username
            it.password = pgContainer.password
        }.connection
    }

    override fun afterSpec(spec: Spec) {
        super.afterSpec(spec)
        connection.close()
    }

    init {
        "Query class generator" {
            val generatedFileContent =
                codegen(connection, "select * from employees where first_name = :name order by :field", "com.foo", "Foo")

            generatedFileContent shouldBe "/gen/query-class-generator.expected.txt".readAsResource()
        }

        "Command class generator" {
            val generatedFileContent = codegen(
                connection,
                "insert into employees (first_name,last_name) values (:firstName, :lastName)",
                "com.foo",
                "Foo"
            )

            generatedFileContent shouldBe "/gen/command-class-generator.expected.txt".readAsResource()
        }

        "Should generate query parameter with Array type while using ANY operator" {
            val generatedFileContent =
                codegen(connection, "select * from  employees where id = ANY(:id) ", "com.foo", "Foo")

            generatedFileContent shouldBe "/gen/array-any.expected.txt".readAsResource()
        }

        "should accept array as parameter while searching inside array using @> contains operator" {
            val generatedFileContent =
                codegen(connection, "SELECT * FROM combinations WHERE colors  @> :colors ;", "com.foo", "Foo")

            generatedFileContent shouldBe "/gen/array-contains.expected.txt".readAsResource()
        }

        "should support jsonb type along with array" {

            val generatedFileContent =
                codegen(connection, "insert into owners(colors,details) VALUES(:colors,:details)", "com.foo", "Foo")

            generatedFileContent shouldBe "/gen/jsonb-and-array.expected.txt".readAsResource()
        }

        "should support custom java8 time data type" {
            val generatedFileContent = codegen(
                connection,
                "insert into time_travel_log(from_time,to_time,duration) VALUES(:fromTime,:toTime,:duration)",
                "com.foo",
                "Foo"
            )

            generatedFileContent shouldBe "/gen/date-time.expected.txt".readAsResource()
        }

        "should correctly map array columns" {
            val generatedFileContent = codegen(connection, "select * from owners", "com.foo", "Foo")

            generatedFileContent shouldBe "/gen/array-columns-mapping.expected.txt".readAsResource()
        }

        "should generate empty params class if inputs params are not present" {
            val generatedFileContent = codegen(connection, "select * from  employees", "com.foo", "Foo")

            generatedFileContent shouldNotContain "data class FooParams"
            generatedFileContent shouldContain "class FooParams"

            generatedFileContent shouldBe "/gen/empty-params-class.expected.txt".readAsResource()
        }

        "should generate nullable fields for the columns of the left joined table" {
            val query = """
                select e.*, d.id as department_id, d.name, c.id as combinations_id, c.colors
                from employees e
                left join departments d on e.id = d.id
                left join combinations c on c.id = d.id
            """.trimIndent()
            val generatedFileContent = codegen(connection, query, "com.foo", "Foo")
            println(generatedFileContent)
            generatedFileContent shouldBe "/gen/left-joined-nullable-check.expected.txt".readAsResource()
        }
    }
}
