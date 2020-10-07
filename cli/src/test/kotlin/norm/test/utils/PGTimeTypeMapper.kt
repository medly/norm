package norm.test.utils

import norm.typemapper.DbToKtTypeMapper
import java.time.LocalTime
import java.time.OffsetDateTime
import kotlin.reflect.KClass

class PGTimeTypeMapper : DbToKtTypeMapper {
    override fun accepts(type: String): Boolean {
        return type.equals("timestamptz", true).or(
            type.equals("time", true)
        )
    }

    override fun getType(type: String): KClass<*> {
        return when (type) {
            "time" -> LocalTime::class
            else -> OffsetDateTime::class
        }
    }
}
