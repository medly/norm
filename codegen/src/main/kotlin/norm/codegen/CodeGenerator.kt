package norm.codegen

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.FileSpec.Builder
import norm.model.*

object CodeGenerator {

    fun generate(sqlModels: List<SqlModel>, packageName: String, baseName: String): String {
        val fileBuilder = FileSpec.builder(packageName, baseName)
        var resultClassName: String? = null
        sqlModels.map { sqlModel ->
            val paramDetails = ParamBuilder.build(baseName, fileBuilder, packageName, sqlModel.params)
            Sql.make(sqlModel.cols).generate(baseName, fileBuilder, packageName, sqlModel, paramDetails)
            if (sqlModel.cols.isNotEmpty() && resultClassName.isNullOrEmpty()) {
                resultClassName = "${baseName}Result"
                buildResult(fileBuilder, packageName, resultClassName!!, sqlModel.cols)
            }
        }
        return fileBuilder.build().toString()
    }

    private fun buildResult(
        fileBuilder: Builder,
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
                                    it.getTypeName()
                                ).build()
                            }
                        ).build()
                )
                .addProperties(
                    cols.map {
                        PropertySpec.builder(it.fieldName, it.getTypeName())
                            .initializer(it.fieldName)
                            .build()
                    }
                )
                .build()
        )
    }
}
