package com.faendir.discord4j.command.parser.parameter

import com.faendir.discord4j.command.annotation.Converter
import com.faendir.discord4j.command.parser.asTypeName
import com.faendir.discord4j.command.parser.findAnnotationTypeProperty
import com.faendir.discord4j.command.parser.isPrimitive
import com.faendir.discord4j.command.parser.nonnull
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.STRING

object ParameterFactory {

    fun createParameters(params: List<KSValueParameter>, logger: KSPLogger) : List<Parameter> {
        return params.withIndex().map { (index, parameter) ->
            val type = parameter.type.resolve()
            val converter = parameter.findAnnotationTypeProperty(Converter::value)
            when {
                converter != null -> CustomParameter(parameter, index, converter)
                (type.declaration as KSClassDeclaration).classKind == ClassKind.ENUM_CLASS -> EnumParameter(parameter, index)
                else -> when(type.asTypeName().nonnull) {
                    INT -> IntParameter(parameter, index)
                    BOOLEAN -> BooleanParameter(parameter, index)
                    STRING -> StringParameter(parameter, index)
                    else -> {
                        logger.error("Custom type must be annotated with @${Converter::class.java.simpleName}", parameter)
                        throw IllegalArgumentException()
                    }
                }
            }
        }
    }
}