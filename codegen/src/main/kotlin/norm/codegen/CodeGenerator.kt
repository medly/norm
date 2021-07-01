package norm.codegen

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import norm.model.*
import norm.typemapper.DbToKtTypeMapperFactory

class CodeGenerator(private val typeMapper: DbToKtTypeMapperFactory = DbToKtTypeMapperFactory) {

    fun generate(sqlModels: List<SqlModel>, packageName: String, baseName: String): String {
        val fileBuilder = FileSpec.builder(packageName, baseName)
        var resultClassName: String? = null
        sqlModels.map { sqlModel ->
            val paramDetails = generateParamDetails(baseName, fileBuilder, packageName, sqlModel.params)
            if (sqlModel.cols.isEmpty()) generateCommand(
                baseName,
                fileBuilder,
                packageName,
                sqlModel.preparableStatement,
                paramDetails
            )
            else {
                if (resultClassName.isNullOrEmpty()) {
                    resultClassName = "${baseName}Result"
                    buildResult(fileBuilder, packageName, resultClassName!!, sqlModel.cols)
                }
                generateQuery(
                    sqlModel,
                    packageName,
                    baseName,
                    fileBuilder,
                    resultClassName!!,
                    paramDetails
                )
            }
        }
        return fileBuilder.build().toString()
    }

    private fun generateQuery(
        sqlModel: SqlModel,
        packageName: String,
        baseName: String,
        fileBuilder: FileSpec.Builder,
        resultClassName: String,
        paramDetails: ParamDetails
    ) {
        val queryClassName = "${baseName}Query"
        val rowMapperClassName = "${baseName}RowMapper"

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

    private fun generateParamDetails(
        baseName: String,
        fileBuilder: FileSpec.Builder,
        packageName: String,
        params: List<ParamModel>
    ): ParamDetails {
        val paramsClassName = "${baseName}Params"
        val paramSetterClassName = "${baseName}ParamSetter"

        buildParam(fileBuilder, packageName, paramsClassName, params)
        buildParamSetter(fileBuilder, packageName, paramSetterClassName, paramsClassName, params)

        return ParamDetails(paramsClassName, paramSetterClassName)
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

    private fun buildResult(
        fileBuilder: FileSpec.Builder,
        packageName: String,
        resultClassName: String,
        cols: List<ColumnModel>
    ) {
        fileBuilder.addType(
            TypeSpec.classBuilder(ClassName(packageName, resultClassName))
                .addModifiers(KModifier.DATA)
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameters(
                            cols.map {
                                ParameterSpec.builder(
                                    it.fieldName,
                                    getTypeName(it)
                                ).build()
                            }
                        ).build()
                )
                .addProperties(
                    cols.map {
                        PropertySpec.builder(it.fieldName, getTypeName(it))
                            .initializer(it.fieldName)
                            .build()
                    }
                )
                .build()
        )
    }

    private fun generateCommand(
        baseName: String,
        fileBuilder: FileSpec.Builder,
        packageName: String,
        preparableStmt: String,
        paramDetails: ParamDetails
    ) {
        val commandClassName = "${baseName}Command"

        fileBuilder.addType(
            TypeSpec.classBuilder(ClassName(packageName, commandClassName))
                .addSuperinterface(
                    ClassName("norm", "Command")
                        .parameterizedBy(ClassName(packageName, paramDetails.paramClassName))
                )
                .addProperty(
                    PropertySpec.builder("sql", String::class)
                        .addModifiers(KModifier.OVERRIDE)
                        .initializer("%S", preparableStmt)
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

    private fun buildParamSetter(
        fileBuilder: FileSpec.Builder,
        packageName: String,
        paramSetterClassName: String,
        paramsClassName: String,
        params: List<ParamModel>
    ) {
        fileBuilder.addType(
            TypeSpec.classBuilder(ClassName(packageName, paramSetterClassName))
                .addSuperinterface(
                    ClassName("norm", "ParamSetter")
                        .parameterizedBy(ClassName(packageName, paramsClassName))
                )
                .addFunction(
                    FunSpec.builder("map")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("ps", ClassName("java.sql", "PreparedStatement"))
                        .addParameter("params", ClassName(packageName, paramsClassName))
                        .also { addStatementsForParams(it, params) }
                        .build()
                )
                .build()
        )
    }

    private fun buildParam(
        fileBuilder: FileSpec.Builder,
        packageName: String,
        paramsClassName: String,
        params: List<ParamModel>
    ) {
        fileBuilder.addType(
            TypeSpec.classBuilder(ClassName(packageName, paramsClassName))
                .also { if (params.isNotEmpty()) it.addModifiers(KModifier.DATA) }
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameters(
                            params.distinctBy { it.name }.map {
                                ParameterSpec.builder(it.name, getTypeName(it)).build()
                            }
                        ).build()
                )
                .addProperties(
                    params.distinctBy { it.name }.map {
                        PropertySpec.builder(it.name, getTypeName(it))
                            .initializer(it.name)
                            .build()
                    }
                )
                .build()
        )
    }

    private fun getTypeName(it: ColumnModel) =
        if (it.colType.startsWith("_")) ARRAY.parameterizedBy(typeMapper.getType(it.colType, false))
            .copy(nullable = it.isNullable)
        else typeMapper.getType(it.colType, it.isNullable)

    private fun getTypeName(it: ParamModel) =
        if (it.dbType.startsWith("_")) ARRAY.parameterizedBy(typeMapper.getType(it.dbType, false))
            .copy(nullable = it.isNullable)
        else typeMapper.getType(it.dbType, it.isNullable)

    private fun addStatementsForParams(fb: FunSpec.Builder, params: List<ParamModel>) =
        params.forEachIndexed { i, pm ->
            when {
                pm.dbType.startsWith("_") -> fb.addStatement(
                    "ps.setArray(${i + 1}, ps.connection.createArrayOf(\"${
                        pm.dbType.removePrefix(
                            "_"
                        )
                    }\", params.${pm.name}))"
                )
                else -> fb.addStatement("ps.setObject(${i + 1}, params.${pm.name})")
            }
        }
}
