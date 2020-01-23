package norm

import io.kotlintest.Spec
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.extensions.TopLevelTest
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.specs.StringSpec
import org.postgresql.ds.PGSimpleDataSource
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CodeGeneratorTest : StringSpec() {

    val USER_NAME = "postgres"
    val PG_HOST = "localhost"
    val PG_PORT = 5432
    val CATALOG_NAME = "tango_norm"
    val SCHEMA_NAME = "norm_test_schema"
    val TABLE_NAME = "PERSON"
    val SCHEMA_TABLE_NAME = "$SCHEMA_NAME.$TABLE_NAME"
    val CREATE_SCHEMA_COMMAND = "CREATE SCHEMA IF NOT EXISTS $SCHEMA_NAME"
    val DROP_SCHEMA_COMMAND = "DROP SCHEMA IF EXISTS $SCHEMA_NAME CASCADE"
    val CREATE_TABLE_COMMAND = "CREATE TABLE IF NOT EXISTS $SCHEMA_TABLE_NAME (ID INTEGER PRIMARY KEY, NAME VARCHAR(50), EMAIL VARCHAR(100), DOB DATE, EXTRA_INFO JSONB) "
    val TRUNCATE_TABLE_COMMAND = "TRUNCATE TABLE $SCHEMA_TABLE_NAME"
    val INSERT_COMMAND = "INSERT INTO $SCHEMA_TABLE_NAME (ID, NAME, EMAIL, DOB) VALUES (?,?,?,?) ON CONFLICT DO NOTHING" // can update

    val dataSource = PGSimpleDataSource().also {
        it.setUrl("jdbc:postgresql://$PG_HOST:$PG_PORT/$CATALOG_NAME")
        it.currentSchema = SCHEMA_NAME
        it.user = USER_NAME
    }

    val recordsToBeInserted = """
        1,Garnet Braitling,gbraitling0@goodreads.com,1981-03-19
        2,Art Stollman,astollman1@wordpress.com,1980-07-10
        3,Carla Shakspeare,cshakspeare2@ox.ac.uk,1973-05-02
        4,Ginnifer Oakinfold,goakinfold3@ocn.ne.jp,1981-05-16
        5,Augusta Coursor,acoursor4@discuz.net,1991-11-16
        6,Veriee Loadman,vloadman5@pbs.org,1970-08-25
        7,Abbie Skeels,askeels6@wunderground.com,1993-06-25
        8,Sly Berg,sberg7@google.nl,1977-08-04
        9,Parry Asple,pasple8@fastcompany.com,1983-11-09
        10,Pegeen Crowden,pcrowden9@hostgator.com,1976-12-26
    """.trimIndent()
        .lineSequence()
        .map { it.split(",") }
        .map { list -> listOf(list[0].toInt(), list[1], list[2], isoDate(list[3])) }
        .toList()

    fun isoDate(string: String): LocalDate = LocalDate.parse(string, DateTimeFormatter.ISO_DATE)


    override fun beforeSpecClass(spec: Spec, tests: List<TopLevelTest>) {
        super.beforeSpecClass(spec, tests)
        dataSource.connection.use {
            it.executeCommand(CREATE_SCHEMA_COMMAND)
            it.executeCommand(CREATE_TABLE_COMMAND)
            it.batchExecuteCommand(INSERT_COMMAND, recordsToBeInserted)
        }
    }


    override fun afterSpecClass(spec: Spec, results: Map<TestCase, TestResult>) {
        super.afterSpecClass(spec, results)
        dataSource.connection.executeCommand(TRUNCATE_TABLE_COMMAND)
    }

    init {
        "Query class generator" {
            dataSource.connection.use {
                val generatedFileContent = codegen(it, "select * from $TABLE_NAME where name = :name order by :field", "com.foo", "Foo")

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
                val generatedFileContent = codegen(it, "insert into $TABLE_NAME (name, email, dob) values (:name, :email, :dob)", "com.foo", "Foo")
                generatedFileContent shouldContain "data class FooParams("
                generatedFileContent shouldContain "class FooParamSetter : ParamSetter<FooParams> {"
                generatedFileContent shouldContain "class FooCommand : Command<FooParams> {"

                println(generatedFileContent)
            }
        }

        "Should generate query parameter with Array type while using ANY operator"{

            dataSource.connection.use {
                val generatedFileContent = codegen(it, "select * from  $TABLE_NAME where id = ANY(:id) ","com.foo","Foo")
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
