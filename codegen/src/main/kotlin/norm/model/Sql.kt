package norm.model

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.FileSpec.Builder
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import norm.typemapper.DbToKtTypeMapperFactory

interface Sql {
    fun generate(
        baseName: String,
        fileBuilder: Builder,
        packageName: String,
        sqlModel: SqlModel,
        paramDetails: ParamDetails
    )

    companion object {
        fun make(col: List<ColumnModel>): Sql = if(col.isEmpty()) Command else Query
    }
}
