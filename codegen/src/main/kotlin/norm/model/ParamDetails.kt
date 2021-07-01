package norm.model

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

data class ParamDetails(val paramClassName: String, val paramSetterClassName: String) {
    companion object {
        fun make(baseName: String, fileBuilder: FileSpec.Builder, packageName: String, params: List<ParamModel>): ParamDetails {
            val paramsClassName = "${baseName}Params"
            val paramSetterClassName = "${baseName}ParamSetter"

            buildParam(fileBuilder, packageName, paramsClassName, params)
            buildParamSetter(fileBuilder, packageName, paramSetterClassName, paramsClassName, params)

            return ParamDetails(paramsClassName, paramSetterClassName)
        }

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
                                    ParameterSpec.builder(it.name, it.getTypeName()).build()
                                }
                            ).build()
                    )
                    .addProperties(
                        params.distinctBy { it.name }.map {
                            PropertySpec.builder(it.name, it.getTypeName())
                                .initializer(it.name)
                                .build()
                        }
                    )
                    .build()
            )
        }
    }
}
