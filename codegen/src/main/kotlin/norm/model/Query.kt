package norm.model

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import norm.typemapper.DbToKtTypeMapperFactory

object Query : Sql {
    override fun generate(
        baseName: String,
        fileBuilder: FileSpec.Builder,
        packageName: String,
        sqlModel: SqlModel,
        paramDetails: ParamDetails
    ) {
        val queryClassName = "${baseName}Query"
        val rowMapperClassName = "${baseName}RowMapper"
        val resultClassName = "${baseName}Result"

        val constructArgs = "\n" + sqlModel.cols.joinToString(",\n  ") {
            if (it.colType.startsWith("_"))
                "${it.fieldName} = rs.getArray(\"${it.colName}\")${if (it.isNullable) "?" else ""}.array as ${
                    getTypeName(
                        it
                    )
                }"
            else
                "${it.fieldName} = rs.getObject(\"${it.colName}\") as ${getTypeName(it)}"
        }

        val queryDetails = QueryDetails(queryClassName, resultClassName, rowMapperClassName)
        buildRowMapper(fileBuilder, packageName, queryDetails, constructArgs)
        buildQuery(fileBuilder, packageName, sqlModel.preparableStatement, paramDetails, queryDetails)
    }

    private fun getTypeName(it: ColumnModel) =
        if (it.colType.startsWith("_")) ARRAY.parameterizedBy(DbToKtTypeMapperFactory.getType(it.colType, false))
            .copy(nullable = it.isNullable)
        else DbToKtTypeMapperFactory.getType(it.colType, it.isNullable)

    private fun buildRowMapper(
        fileBuilder: FileSpec.Builder,
        packageName: String,
        queryDetails: QueryDetails,
        constructArgs: String
    ) {
        fileBuilder.addType(
            TypeSpec.classBuilder(ClassName(packageName, queryDetails.rowMapperClassName))
                .addSuperinterface(
                    ClassName("norm", "RowMapper")
                        .parameterizedBy(ClassName(packageName, queryDetails.resultClassName))
                )
                .addFunction(
                    FunSpec.builder("map")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("rs", ClassName("java.sql", "ResultSet"))
                        .addStatement("return %T($constructArgs)", ClassName(packageName, queryDetails.resultClassName))
                        .returns(ClassName(packageName, queryDetails.resultClassName))
                        .build()
                )
                .build()
        )
    }

    private fun buildQuery(
        fileBuilder: FileSpec.Builder,
        packageName: String,
        preparableStmt: String,
        paramDetails: ParamDetails,
        queryDetails: QueryDetails
    ) {
        fileBuilder.addType(
            TypeSpec.classBuilder(ClassName(packageName, queryDetails.queryClassName))
                .addSuperinterface(
                    ClassName("norm", "Query")
                        .parameterizedBy(
                            ClassName(packageName, paramDetails.paramClassName),
                            ClassName(packageName, queryDetails.resultClassName)
                        )
                )
                .addProperty(
                    PropertySpec.builder("sql", String::class)
                        .addModifiers(KModifier.OVERRIDE)
                        .initializer("%S", preparableStmt)
                        .build()
                ).addProperty(
                    PropertySpec.builder(
                        "mapper",
                        ClassName("norm", "RowMapper")
                            .parameterizedBy(ClassName(packageName, queryDetails.resultClassName))
                    )
                        .addModifiers(KModifier.OVERRIDE)
                        .initializer("%T()", ClassName(packageName, queryDetails.rowMapperClassName))
                        .build()
                )
                .addProperty(
                    PropertySpec.builder(
                        "paramSetter",
                        ClassName("norm", "ParamSetter")
                            .parameterizedBy(ClassName(packageName, paramDetails.paramClassName))
                    )
                        .addModifiers(KModifier.OVERRIDE)
                        .initializer("%T()", ClassName(packageName, paramDetails.paramSetterClassName))
                        .build()
                )
                .build()
        )
    }

}
