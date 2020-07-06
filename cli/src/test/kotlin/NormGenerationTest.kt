import exit.assertion.ExitAssertions
import io.kotlintest.Spec
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import norm.cli.main
import org.junit.ClassRule
import org.testcontainers.containers.PostgreSQLContainer
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream


class MyPostgreSQLContainer : PostgreSQLContainer<MyPostgreSQLContainer>()


fun toArgs(str: String): Array<String> = str.split(" ").toTypedArray()

class NormGenerationTest : StringSpec() {

    @ClassRule
    private val postgreSQLContainer = MyPostgreSQLContainer().withInitScript("init_postgres.sql")
    private val outputDir = "build/gen"
    private val byteArrayOutputStream = ByteArrayOutputStream()

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        System.setErr(PrintStream(byteArrayOutputStream))
    }

    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)
        File(outputDir).mkdir()
    }

    override fun afterSpec(spec: Spec) {
        super.afterSpec(spec)
        System.setErr(System.err)
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        super.afterTest(testCase, result)
        File(outputDir).deleteRecursively()
        byteArrayOutputStream.reset()
    }

    init {
        postgreSQLContainer.start()

        val url =postgreSQLContainer.jdbcUrl
        val username = postgreSQLContainer.username
        val password = postgreSQLContainer.password

        val pgStr = "-j $url -u $username -p $password"

        "should generate kotlin file for all sql files" {
            val args = toArgs("-d src/test/resources/sql -b src/test/resources/sql -o $outputDir $pgStr")

            ExitAssertions.assertExits<Throwable>(1) { main(args) }

            File("$outputDir/employees/AddNewEmployee.kt").exists() shouldBe true
            File("$outputDir/departments/AddNewDepartment.kt").exists() shouldBe true

            val out = byteArrayOutputStream.toString()
            println(out)

        }

        "should generate kotlin file for specified files only" {
            val args = toArgs("-f src/test/resources/sql/employees/add-new-employee.sql -b src/test/resources/sql/ -o $outputDir $pgStr" )

            ExitAssertions.assertExits<Throwable>(1) { main(args) }

            File("$outputDir/employees/AddNewEmployee.kt").exists() shouldBe true
            File("$outputDir/departments/AddNewDepartment.kt").exists() shouldBe false

            val out = byteArrayOutputStream.toString()
            println(out)
        }

        "should exit when source directory is not a directory" {
            val args = toArgs("-d src/test/resources/init_postgres.sql -o $outputDir $pgStr")

            ExitAssertions.assertExits<Throwable>(1) { main(args) }

            val out = byteArrayOutputStream.toString()
            println(out)
            out shouldContain "Error: Invalid value for \"-d\":"
        }

        "should exit when output directory is not a directory" {
            val args = toArgs("-f src/test/resources/sql -o src/test/resources/init_postgres.sql $pgStr")

            ExitAssertions.assertExits<Throwable>(1) { main(args) }

            val out = byteArrayOutputStream.toString()
            println(out)
            out shouldContain "Error: Invalid value for \"-f\":"
        }
    }
}


