package com.faendir.discord4j.command.parser.parameter

import com.faendir.discord4j.command.annotation.Converter
import com.faendir.discord4j.command.annotation.Description
import com.faendir.discord4j.command.annotation.Name
import com.faendir.discord4j.command.annotation.Required
import com.faendir.discord4j.command.parser.asTypeName
import com.faendir.discord4j.command.parser.findAnnotationProperty
import com.faendir.discord4j.command.parser.findAnnotationTypeProperty
import com.faendir.discord4j.command.parser.hasAnnotation
import com.faendir.discord4j.command.parser.nonnull
import com.faendir.discord4j.command.parser.toRawType
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.INT
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.rest.util.ApplicationCommandOptionType
import io.github.enjoydambience.kotlinbard.codeBlock
import net.pearx.kasechange.toKebabCase

class Parameter(private val parameter: KSValueParameter, private val index: Int) {
    private val type = parameter.type.resolve()
    private val typeDeclaration = type.declaration as KSClassDeclaration
    private val isEnum = typeDeclaration.classKind == ClassKind.ENUM_CLASS
    private val typeName = type.asTypeName()
    private val name = parameter.name?.asString() ?: "var$index"
    private val converter = parameter.findAnnotationTypeProperty(Converter::value)
    val isRequired = !typeName.isNullable || parameter.hasAnnotation<Required>()

    fun buildData(): CodeBlock = codeBlock {
        add("%T.builder()\n", ApplicationCommandOptionData::class)
        val name = parameter.findAnnotationProperty(Name::value) ?: name.toKebabCase()
        indent()
        add(".name(%S)\n", name)
        add(".description(%S)\n", parameter.findAnnotationProperty(Description::value) ?: name)
        if (isRequired) {
            add(".required(true)\n")
        }
        add(
            ".type(%T.%L.value)\n", ApplicationCommandOptionType::class, when (typeName.nonnull) {
                INT -> ApplicationCommandOptionType.INTEGER
                BOOLEAN -> ApplicationCommandOptionType.BOOLEAN
                else -> ApplicationCommandOptionType.STRING
            }.name
        )
        if (isEnum) {
            for (enumValue in typeDeclaration.declarations.filterIsInstance<KSClassDeclaration>()) {
                val value = enumValue.simpleName.asString()
                add(
                    ".addChoice(%T.builder().name(%S).value(%S).build())\n",
                    ApplicationCommandOptionChoiceData::class,
                    enumValue.findAnnotationProperty(Name::value) ?: value.toKebabCase(),
                    value
                )
            }
        }
        add(".build()")
        unindent()
    }

    fun passToConstructor(): CodeBlock = codeBlock {
        add(
            "options.first·{ %S == it.name }.value.%L.%L", name.toKebabCase(), when (isRequired) {
                true -> "get()"
                false -> "orElse(null)?"
            },
            when {
                typeName.nonnull == INT -> "asLong().toInt()"
                typeName.nonnull == BOOLEAN -> "asBoolean()"
                converter != null -> CodeBlock.of("asString().let·{ %T().fromString(it) }", converter.asTypeName())
                isEnum -> CodeBlock.of("asString().let·{ %T.valueOf(it) }", typeName)
                else -> "asString()"
            }
        )
    }
}