package norm.typemapper

import kotlin.reflect.KClass

interface DbToKtTypeMapper {
    fun accepts(type: String): Boolean
    fun getType(type: String): KClass<*>
}
