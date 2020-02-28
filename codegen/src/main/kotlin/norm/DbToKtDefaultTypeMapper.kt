package norm

import com.squareup.kotlinpoet.ARRAY
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.postgresql.util.PGobject
import java.math.BigDecimal
import kotlin.reflect.KClass

typealias typeMapper = (String) -> KClass<*>

class DbToKtDefaultTypeMapper {
    fun getType(colType: String): KClass<*> {
        return when (colType.toLowerCase()) {
            "int4" -> Int::class
            "int" -> Int::class
            "serial" -> Int::class

            "int8" -> Long::class
            "bigint" -> Long::class
            "bigserial" -> Long::class

            "int16" -> BigDecimal::class
            "numeric" -> BigDecimal::class

            "bool" -> Boolean::class
            "timestamptz" -> java.sql.Timestamp::class
            "date" -> java.sql.Date::class
            "timestamp" -> java.sql.Timestamp::class

            "jsonb" -> PGobject::class

            "varchar" -> String::class
            "text" -> String::class
            "_varchar" -> String::class
            "_int4" -> Int::class
            else -> String::class
        }
    }
}
