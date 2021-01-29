package norm.codegen

import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.testcontainers.perSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import norm.test.utils.PgContainer
import norm.test.utils.codegen
import norm.test.utils.readAsResource
import norm.util.withPgConnection

class CodeGeneratorTest : StringSpec() {

    private val pgContainer: PgContainer = PgContainer().withInitScript("init_postgres.sql")

    override fun listeners() = listOf(pgContainer.perSpec())

    init {
        "Query class generator" {
            withPgConnection(pgContainer.jdbcUrl, pgContainer.username, pgContainer.password) {
                val generatedFileContent = codegen(it, "select * from employees where first_name = :name order by :field", "com.foo", "Foo")

                generatedFileContent shouldBe "/gen/query-class-generator.expected.txt".readAsResource()
            }
        }

        "Command class generator" {
            withPgConnection(pgContainer.jdbcUrl, pgContainer.username, pgContainer.password) {
                val generatedFileContent = codegen(it, "insert into employees (first_name,last_name) values (:firstName, :lastName)", "com.foo", "Foo")

                generatedFileContent shouldBe "/gen/command-class-generator.expected.txt".readAsResource()
            }
        }

        "Should generate query parameter with Array type while using ANY operator" {
            withPgConnection(pgContainer.jdbcUrl, pgContainer.username, pgContainer.password) {
                val generatedFileContent = codegen(it, "select * from  employees where id = ANY(:id) ", "com.foo", "Foo")

                generatedFileContent shouldBe "/gen/array-any.expected.txt".readAsResource()
            }
        }

        "should accept array as parameter while searching inside array using @> contains operator" {
            withPgConnection(pgContainer.jdbcUrl, pgContainer.username, pgContainer.password) {
                val generatedFileContent = codegen(it, "SELECT * FROM combinations WHERE colors  @> :colors ;", "com.foo", "Foo")

                generatedFileContent shouldBe "/gen/array-contains.expected.txt".readAsResource()
            }
        }

        "should support jsonb type along with array" {

            withPgConnection(pgContainer.jdbcUrl, pgContainer.username, pgContainer.password) {
                val generatedFileContent = codegen(it, "insert into owners(colors,details) VALUES(:colors,:details)", "com.foo", "Foo")

                generatedFileContent shouldBe "/gen/jsonb-and-array.expected.txt".readAsResource()
            }
        }

        "should support custom java8 time data type" {
            withPgConnection(pgContainer.jdbcUrl, pgContainer.username, pgContainer.password) {
                val generatedFileContent = codegen(it, "insert into time_travel_log(from_time,to_time,duration) VALUES(:fromTime,:toTime,:duration)", "com.foo", "Foo")

                generatedFileContent shouldBe "/gen/date-time.expected.txt".readAsResource()
            }
        }

        "should correctly map array columns" {
            withPgConnection(pgContainer.jdbcUrl, pgContainer.username, pgContainer.password) {
                val generatedFileContent = codegen(it, "select * from owners", "com.foo", "Foo")

                generatedFileContent shouldBe "/gen/array-columns-mapping.expected.txt".readAsResource()
            }
        }

        "should generate empty params class if inputs params are not present" {
            withPgConnection(pgContainer.jdbcUrl, pgContainer.username, pgContainer.password) {
                val generatedFileContent = codegen(it, "select * from  employees", "com.foo", "Foo")

                generatedFileContent shouldNotContain "data class FooParams"
                generatedFileContent shouldContain "class FooParams"

                generatedFileContent shouldBe "/gen/empty-params-class.expected.txt".readAsResource()
            }
        }
    }
}
