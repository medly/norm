package norm

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

class CodeGenerator(private val typeMapper: DbToKtDefaultTypeMapper = DbToKtDefaultTypeMapper()) {

    fun generate(sqlModel: SqlModel, packageName: String, baseName: String): String {

        val (params, cols, preparableStmt) = sqlModel
        val paramsClassName = "${baseName}Params"
        val paramSetterClassName = "${baseName}ParamSetter"

        val fileBuilder = FileSpec.builder(packageName, baseName)

        fileBuilder.addType(
            TypeSpec.classBuilder(ClassName(packageName, paramsClassName))
                .addModifiers(KModifier.DATA)
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameters(params.distinctBy { it.name }.map {
                            ParameterSpec.builder(
                                it.name,
                                if (it.dbType.startsWith("_"))
                                    ARRAY.parameterizedBy(typeMapper.getType(it.dbType).asTypeName()).copy(nullable = it.isNullable)
                                else typeMapper.getType(it.dbType).asTypeName().copy(nullable = it.isNullable)
                            ).build()
                        }).build()
                )
                .addProperties(params.distinctBy { it.name }.map {
                    PropertySpec.builder(it.name,
                        if (it.dbType.startsWith("_"))
                            ARRAY.parameterizedBy(typeMapper.getType(it.dbType).asTypeName()).copy(nullable = it.isNullable)
                        else typeMapper.getType(it.dbType).asTypeName().copy(nullable = it.isNullable)

                    )
                        .initializer(it.name)
                        .build()
                })
                .build()
        )

        fileBuilder.addType(
            TypeSpec.classBuilder(ClassName(packageName, paramSetterClassName))
                .addSuperinterface(
                    ClassName("norm", "ParamSetter")
                        .parameterizedBy(ClassName(packageName, paramsClassName))
                )
                .addFunction(FunSpec.builder("map")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("ps", ClassName("java.sql", "PreparedStatement"))
                    .addParameter("params", ClassName(packageName, paramsClassName))
                    .also { addStatementsForParams(it, params) }
                    .build())
                .build()
        )

        if (cols.isEmpty()) { // command
            val commandClassName = "${baseName}Command"

            fileBuilder.addType(
                TypeSpec.classBuilder(ClassName(packageName, commandClassName))
                    .addSuperinterface(
                        ClassName("norm", "Command")
                            .parameterizedBy(ClassName(packageName, paramsClassName))
                    )
                    .addProperty(
                        PropertySpec.builder("sql", String::class)
                            .addModifiers(KModifier.OVERRIDE)
                            .initializer("%S", preparableStmt)
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("paramSetter",
                            ClassName("norm", "ParamSetter")
                                .parameterizedBy(ClassName(packageName, paramsClassName))
                        )
                            .addModifiers(KModifier.OVERRIDE)
                            .initializer("%T()", ClassName(packageName, paramSetterClassName))
                            .build()
                    )
                    .build()
            )
        } else { // query
            val resultClassName = "${baseName}Result"
            val queryClassName = "${baseName}Query"
            val rowMapperClassName = "${baseName}RowMapper"

            fileBuilder.addType(
                TypeSpec.classBuilder(ClassName(packageName, resultClassName))
                    .addModifiers(KModifier.DATA)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameters(cols.map {
                                ParameterSpec.builder(it.fieldName, typeMapper.getType(it.colType)).build()
                            }).build()
                    )
                    .addProperties(cols.map {
                        PropertySpec.builder(it.fieldName, typeMapper.getType(it.colType))
                            .initializer(it.fieldName)
                            .build()
                    })
                    .build()
            )

            val constructArgs = "\n" + cols.map {
                "${it.fieldName} = rs.getObject(\"${it.colName}\") as ${typeMapper.getType(it.colType).asClassName().canonicalName}"
            }.joinToString(",\n  ")

            fileBuilder.addType(
                TypeSpec.classBuilder(ClassName(packageName, rowMapperClassName))
                    .addSuperinterface(
                        ClassName("norm", "RowMapper")
                            .parameterizedBy(ClassName(packageName, resultClassName))
                    )
                    .addFunction(FunSpec.builder("map")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("rs", ClassName("java.sql", "ResultSet"))
                        .addStatement("return %T($constructArgs)", ClassName(packageName, resultClassName))
                        .returns(ClassName(packageName, resultClassName))
                        .build())
                    .build()
            )

            fileBuilder.addType(
                TypeSpec.classBuilder(ClassName(packageName, queryClassName))
                    .addSuperinterface(
                        ClassName("norm", "Query")
                            .parameterizedBy(
                                ClassName(packageName, paramsClassName),
                                ClassName(packageName, resultClassName))
                    )
                    .addProperty(
                        PropertySpec.builder("sql", String::class)
                            .addModifiers(KModifier.OVERRIDE)
                            .initializer("%S", preparableStmt)
                            .build()
                    ).addProperty(
                        PropertySpec.builder("mapper",
                            ClassName("norm", "RowMapper")
                                .parameterizedBy(ClassName(packageName, resultClassName))
                        )
                            .addModifiers(KModifier.OVERRIDE)
                            .initializer("%T()", ClassName(packageName, rowMapperClassName))
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("paramSetter",
                            ClassName("norm", "ParamSetter")
                                .parameterizedBy(ClassName(packageName, paramsClassName))
                        )
                            .addModifiers(KModifier.OVERRIDE)
                            .initializer("%T()", ClassName(packageName, paramSetterClassName))
                            .build()
                    )
                    .build()
            )
        }

        return fileBuilder.build().toString()
    }

    private fun addStatementsForParams(fb: FunSpec.Builder, params: List<ParamModel>) =
        params.forEachIndexed { i, pm ->
            when (pm.paramClassName) {
                "java.sql.Array" -> fb.addStatement("ps.setArray(${i + 1}, ps.connection.createArrayOf(\"${typeMapper.getType(pm.dbType).asClassName().simpleName.toUpperCase()}\", params.${pm.name}))")
                else -> fb.addStatement("ps.setObject(${i + 1}, params.${pm.name})")
            }

        }
}


