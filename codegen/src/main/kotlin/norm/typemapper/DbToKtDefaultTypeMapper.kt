package norm.typemapper

import java.math.BigDecimal
import java.util.*

import norm.api.typemapper.DbToKtTypeMapper
import org.postgresql.util.PGobject
import kotlin.reflect.KClass


class DbToKtDefaultTypeMapper : DbToKtTypeMapper {

    override fun accepts(type: String): Boolean = true

    override fun getType(type: String): KClass<*> {

        val dataType = type.removePrefix(ARRAY_TYPE_PREFIX)

        return when (dataType.lowercase(Locale.getDefault())) {
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

            "uuid" -> java.util.UUID::class

            "varchar" -> String::class
            "text" -> String::class
            else -> String::class
        }
    }
}

const val ARRAY_TYPE_PREFIX = "_"
