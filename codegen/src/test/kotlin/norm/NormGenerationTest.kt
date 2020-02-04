package norm

import exit.assertion.ExitAssertions
import io.kotlintest.Spec
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.junit.ClassRule
import org.testcontainers.containers.PostgreSQLContainer
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream


class MyPostgreSQLContainer : PostgreSQLContainer<MyPostgreSQLContainer>()

class NormGenerationTest : StringSpec() {

    @ClassRule
    private val postgreSQLContainer = MyPostgreSQLContainer().withInitScript("init_postgres.sql")
    private val outputDir = "src/test/resources/gen"
    private val byteArrayOutputStream = ByteArrayOutputStream()

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        System.setOut(PrintStream(byteArrayOutputStream))
    }

    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)
        File(outputDir).mkdir()
    }

    override fun afterSpec(spec: Spec) {
        super.afterSpec(spec)
        System.setOut(System.out)
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        super.afterTest(testCase, result)
        File(outputDir).deleteRecursively()
        System.clearProperty("scripts")
        byteArrayOutputStream.reset()
    }

    init {
        postgreSQLContainer.start()

        "should generate kotlin file for all sql files" {
            val args = arrayOf("src/test/resources/sql", outputDir, postgreSQLContainer.jdbcUrl, postgreSQLContainer.username, postgreSQLContainer.password)

            ExitAssertions.assertExits<Throwable>(0) { main(args) }

            File("$outputDir/employees/AddNewEmployee.kt").exists() shouldBe true
            File("$outputDir/departments/AddNewDepartment.kt").exists() shouldBe true
        }

        "should generate kotlin file for specified files only" {
            val args = arrayOf("src/test/resources/sql", outputDir, postgreSQLContainer.jdbcUrl, postgreSQLContainer.username, postgreSQLContainer.password)
            System.setProperty("scripts", "src/test/resources/sql/employees/add-new-employee.sql")

            ExitAssertions.assertExits<Throwable>(0) { main(args) }

            File("$outputDir/employees/AddNewEmployee.kt").exists() shouldBe true
            File("$outputDir/departments/AddNewDepartment.kt").exists() shouldBe false
        }

        "should exit when source directory is not a directory" {
            val args = arrayOf("src/test/resources/init_postgres.sql", outputDir, postgreSQLContainer.jdbcUrl, postgreSQLContainer.username, postgreSQLContainer.password)

            ExitAssertions.assertExits<Throwable>(0) { main(args) }

            byteArrayOutputStream.toString() shouldContain "invalid source or output directory"
        }

        "should exit when output directory is not a directory" {
            val args = arrayOf("src/test/resources/sql", "src/test/resources/init_postgres.sql", postgreSQLContainer.jdbcUrl, postgreSQLContainer.username, postgreSQLContainer.password)

            ExitAssertions.assertExits<Throwable>(0) { main(args) }

            byteArrayOutputStream.toString() shouldContain "invalid source or output directory"
        }
    }
}


