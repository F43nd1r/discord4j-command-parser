package com.faendir.discord4j.command.parser.parameter

import com.faendir.discord4j.command.annotation.Description
import com.faendir.discord4j.command.annotation.Name
import com.faendir.discord4j.command.annotation.Required
import com.faendir.discord4j.command.parser.asClassName
import com.faendir.discord4j.command.parser.asTypeName
import com.faendir.discord4j.command.parser.findAnnotationProperty
import com.faendir.discord4j.command.parser.hasAnnotation
import com.faendir.discord4j.command.parser.hasAnnotationWithName
import com.faendir.discord4j.command.parser.isPrimitive
import com.google.devtools.ksp.processing.KSBuiltIns
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Origin
import com.squareup.kotlinpoet.CodeBlock
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.rest.util.ApplicationCommandOptionType
import io.github.enjoydambience.kotlinbard.CodeBlockBuilder
import io.github.enjoydambience.kotlinbard.codeBlock
import net.pearx.kasechange.toKebabCase

abstract class Parameter(private val parameter: KSValueParameter, private val index: Int) {
    val type = parameter.type.resolve()
    val typeName = type.asTypeName()
    val isRequired : Boolean = when (parameter.origin) {
        Origin.KOTLIN, Origin.SYNTHETIC -> !typeName.isNullable || parameter.hasAnnotation<Required>()
        Origin.JAVA, Origin.CLASS -> typeName.isPrimitive || parameter.hasAnnotationWithName("Nonnull", "NonNull", "NotNull", "Required") && !parameter.hasAnnotationWithName("Nullable")
        else -> true
    }

    abstract val optionType: ApplicationCommandOptionType

    fun buildData(): CodeBlock = codeBlock {
        add("%T.builder()\n", ApplicationCommandOptionData::class)
        val name = parameter.findAnnotationProperty(Name::value) ?: (parameter.name?.asString() ?: "var$index").toKebabCase()
        indent()
        add(".name(%S)\n", name)
        add(".description(%S)\n", parameter.findAnnotationProperty(Description::value) ?: name)
        if (isRequired) {
            add(".required(true)\n")
        }
        add(".type(%T.%L.value)\n", ApplicationCommandOptionType::class, optionType.name)
        modifyDataBuilder()
        add(".build()")
        unindent()
    }

    open fun CodeBlockBuilder.modifyDataBuilder() {
    }

    abstract fun convertValue(): CodeBlock

    fun passToConstructor(): CodeBlock = codeBlock {
        add(
            if (isRequired) "options.first·{ %S == it.name }.value.get().%L" else "options.firstOrNull·{ %S == it.name }?.value?.orElse(null)?.%L",
            (parameter.name?.asString() ?: "var$index").toKebabCase(),
            convertValue()
        )
    }
}