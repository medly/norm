package norm.model

import com.squareup.kotlinpoet.ARRAY
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import norm.typemapper.DbToKtTypeMapperFactory

data class ParamModel(
    val name: String,
    val dbType: String,
    val isNullable: Boolean
) {
    fun getTypeName() =
        if (dbType.startsWith("_")) ARRAY.parameterizedBy(DbToKtTypeMapperFactory.getType(dbType, false))
            .copy(nullable = this.isNullable)
        else DbToKtTypeMapperFactory.getType(dbType, isNullable)
}
