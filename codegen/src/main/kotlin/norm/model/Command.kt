package norm.model

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

object Command : Sql {
    override fun generate(
        baseName: String,
        fileBuilder: FileSpec.Builder,
        packageName: String,
        sqlModel: SqlModel,
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
                        .initializer("%S", sqlModel.preparableStatement)
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
