package norm.typemapper

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import java.util.ServiceLoader

object DbToKtTypeMapperFactory {
    private val loader = ServiceLoader.load(DbToKtTypeMapper::class.java)
    private val defaultMapper = DbToKtDefaultTypeMapper()

    fun getType(colType: String, nullable: Boolean): TypeName {
        val mapper = loader.firstOrNull { mapper ->
            mapper.accepts(colType)
        } ?: defaultMapper
        return mapper.getType(colType).asTypeName().copy(nullable = nullable)
    }
}
