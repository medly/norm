package norm.cli

import com.github.ajalt.clikt.core.UsageError
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.extensions.testcontainers.perSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.contain
import io.kotest.matchers.string.startWith
import io.mockk.Runs
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import norm.test.utils.PgContainer
import norm.test.utils.toArgs
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectDatabase
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File

class NormCliTest : StringSpec() {

    private val pgContainer: PgContainer = PgContainer().withInitScript("init_postgres.sql")

    override fun listeners() = listOf(pgContainer.perSpec())

    private val outputDir = "build/gen"

    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)
        File(outputDir).mkdir()
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        super.afterTest(testCase, result)
        File(outputDir).deleteRecursively()
    }

    private fun pgStr() = "-j ${pgContainer.jdbcUrl} -u ${pgContainer.username} -p ${pgContainer.password}"

    init {
        "should generate kotlin file for all sql files" {
            val args = toArgs("-d src/test/resources/sql -b src/test/resources/sql -o $outputDir ${pgStr()}")

            NormCli().parse(args)

            File("$outputDir/employees/AddNewEmployee.kt").exists() shouldBe true
            File("$outputDir/departments/AddNewDepartment.kt").exists() shouldBe true
        }

        "should generate kotlin file for specified files only" {
            val args = toArgs("-f src/test/resources/sql/employees/add-new-employee.sql -b src/test/resources/sql/ -o $outputDir ${pgStr()}")

            NormCli().parse(args)

            File("$outputDir/employees/AddNewEmployee.kt").exists() shouldBe true
            File("$outputDir/departments/AddNewDepartment.kt").exists() shouldBe false
        }

        "should exit when source directory is not a directory" {
            val args = toArgs("-d src/test/resources/init_postgres.sql -o $outputDir ${pgStr()}")

            val exception = shouldThrow<UsageError> {
                NormCli().parse(args)
            }

            exception.message should startWith("Invalid value for \"-d\":")
            exception.message should contain("is a file.")
        }

        "should exit when output directory is not a directory" {
            val args = toArgs("-f src/test/resources/sql -o src/test/resources/init_postgres.sql ${pgStr()}")

            val exception = shouldThrow<UsageError> {
                NormCli().parse(args)
            }

            exception.message should startWith("Invalid value for \"-f\":")
            exception.message should contain("is a directory")
        }
        "Should generate kotlin file for untracked file in git"{

            val git = mockk<Git>(relaxed = true)
            mockkConstructor(Repository::class)
            val mockObjectDB = mockk<ObjectDatabase>(relaxed = true)
            every { git.status().call().untracked } returns emptySet()
            every { git.status().call().modified } returns emptySet()
            every { git.diff().call()} returns emptyList()
            every { anyConstructed<Repository>().objectDatabase } returns mockObjectDB
            every { mockObjectDB.exists() } returns true
            val args = toArgs("-d src/test/resources/gitrepository/sql -b src/test/resources/gitrepository/sql -o $outputDir ${pgStr()}")
            NormCli().parse(args)
            File("$outputDir/employees/AddNewEmployee.kt").exists() shouldBe true

        }
    }
}
