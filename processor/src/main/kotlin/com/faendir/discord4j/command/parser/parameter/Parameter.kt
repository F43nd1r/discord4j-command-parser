package com.faendir.discord4j.command.parser.parameter

import com.faendir.discord4j.command.annotation.Description
import com.faendir.discord4j.command.annotation.Name
import com.faendir.discord4j.command.annotation.Required
import com.faendir.discord4j.command.parser.asTypeName
import com.faendir.discord4j.command.parser.findAnnotationProperty
import com.faendir.discord4j.command.parser.hasAnnotation
import com.faendir.discord4j.command.parser.hasAnnotationWithName
import com.faendir.discord4j.command.parser.isPrimitive
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Origin
import com.squareup.kotlinpoet.CodeBlock
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.rest.util.ApplicationCommandOptionType
import io.github.enjoydambience.kotlinbard.CodeBlockBuilder
import io.github.enjoydambience.kotlinbard.codeBlock
import net.pearx.kasechange.toKebabCase
import reactor.core.publisher.Mono
import java.util.*

abstract class Parameter(private val parameter: KSValueParameter, private val index: Int) {
    val type = parameter.type.resolve()
    val typeName = type.asTypeName()
    val isRequired: Boolean = when (parameter.origin) {
        Origin.KOTLIN, Origin.KOTLIN_LIB, Origin.SYNTHETIC -> !typeName.isNullable || parameter.hasAnnotation<Required>()
        Origin.JAVA, Origin.JAVA_LIB -> typeName.isPrimitive || parameter.hasAnnotationWithName(
            "Nonnull",
            "NonNull",
            "NotNull",
            "Required"
        ) && !parameter.hasAnnotationWithName("Nullable")
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

    abstract fun CodeBlockBuilder.mapToOptional()

    fun CodeBlockBuilder.mapToOptional(convertValue: CodeBlock) {
        add(if (isRequired) ".map·{·value·-> %T.of(value.%L) }\n" else ".map·{·value·-> %T.ofNullable(value?.%L) }\n", Optional::class, convertValue)
    }

    fun toMonoBlock(): CodeBlock = codeBlock {
        add(
            if (isRequired) "%T.fromCallable·{ options.first·{ %S == it.name }.value.get() }\n"
            else "%T.fromCallable·{ options.firstOrNull·{ %S == it.name }?.value?.orElse(null) }\n",
            Mono::class,
            (parameter.name?.asString() ?: "var$index").toKebabCase()
        )
        indent()
        mapToOptional()
        if(!isRequired) {
            add(".switchIfEmpty(%T.just(%T.empty()))\n", Mono::class, Optional::class)
        }
        unindent()
    }
}