package norm.model

import com.squareup.kotlinpoet.ARRAY
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import norm.typemapper.DbToKtTypeMapperFactory

data class ColumnModel(
    val fieldName: String,
    val colType: String,
    val colName: String,
    val isNullable: Boolean
) {
    fun getTypeName() =
        if (colType.startsWith("_")) ARRAY.parameterizedBy(DbToKtTypeMapperFactory.getType(colType, false))
            .copy(nullable = this.isNullable)
        else DbToKtTypeMapperFactory.getType(colType, isNullable)
}
