package com.faendir.discord4j.command.parser

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName

fun KSTypeReference.asTypeName() = resolve().asTypeName()

fun KSType.asTypeName(): TypeName {
    var name: TypeName = asClassName()
    if (arguments.isNotEmpty()) {
        name = (name as ClassName).parameterizedBy(arguments.map { it.type!!.asTypeName() })
    }
    return if (isMarkedNullable) name.copy(true) else name
}

fun KSType.asClassName(): ClassName {
    return ClassName(declaration.normalizedPackageName,
        *generateSequence(declaration.parentDeclaration) { it.parentDeclaration }.map { it.simpleName.asString() }.toList().toTypedArray(),
        declaration.simpleName.asString()
    )
}

fun FileSpec.writeTo(source: KSFile, codeGenerator: CodeGenerator) {
    codeGenerator.createNewFile(Dependencies(false, source), packageName, name).writer().use { writeTo(it) }
}

@Suppress("UNCHECKED_CAST")
val <T : TypeName> T.nonnull: T
    get() = copy(nullable = false) as T

fun TypeName.toRawType(): ClassName = when (this) {
    is ParameterizedTypeName -> this.rawType
    is ClassName -> this
    else -> throw IllegalArgumentException()
}

fun ClassName.withParserSuffix() = ClassName(packageName, "${simpleNames.joinToString("$")}Parser")

fun TypeName.withParserSuffix() = toRawType().withParserSuffix()

fun TypeName.asLambdaReceiver() = LambdaTypeName.get(receiver = this, returnType = Unit::class.asClassName())

private val primitives = listOf(BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BOOLEAN, CHAR)

val TypeName.isPrimitive: Boolean
    get() = !isNullable && primitives.contains(this)