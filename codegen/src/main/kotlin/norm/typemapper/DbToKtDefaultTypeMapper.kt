package norm.typemapper

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import org.postgresql.util.PGobject
import java.math.BigDecimal

class DbToKtDefaultTypeMapper {
    fun getType(colType: String, nullable: Boolean): TypeName {
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
        }.asTypeName().copy(nullable = nullable)
    }
}
