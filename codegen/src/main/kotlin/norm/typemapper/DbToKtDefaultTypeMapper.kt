package norm.typemapper

import org.postgresql.util.PGobject
import java.math.BigDecimal
import kotlin.reflect.KClass

class DbToKtDefaultTypeMapper: DbToKtTypeMapper {

    override fun accepts(type: String): Boolean = true

    override fun getType(type: String): KClass<*> {
        return when (type.toLowerCase()) {
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
            "time" -> java.sql.Time::class
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
