package com.faendir.discord4j.command.parser.parameter

import com.faendir.discord4j.command.annotation.Name
import com.faendir.discord4j.command.parser.findAnnotationProperty
import com.faendir.discord4j.command.parser.nonnull
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.CodeBlock
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData
import discord4j.rest.util.ApplicationCommandOptionType
import io.github.enjoydambience.kotlinbard.CodeBlockBuilder
import io.github.enjoydambience.kotlinbard.codeBlock
import net.pearx.kasechange.toKebabCase

class EnumParameter(parameter: KSValueParameter, index: Int) : Parameter(parameter, index) {
    override val optionType: ApplicationCommandOptionType = ApplicationCommandOptionType.STRING
    private val typeDeclaration = type.declaration as KSClassDeclaration

    override fun CodeBlockBuilder.modifyDataBuilder() {
        for (enumValue in typeDeclaration.declarations.filterIsInstance<KSClassDeclaration>().filter { it.classKind == ClassKind.ENUM_ENTRY }) {
            val value = enumValue.simpleName.asString()
            add(
                ".addChoice(%T.builder().name(%S).value(%S).build())\n",
                ApplicationCommandOptionChoiceData::class,
                enumValue.findAnnotationProperty(Name::value) ?: value.toKebabCase(),
                value
            )
        }
    }

    override fun convertValue(): CodeBlock = codeBlock(if (isRequired) "asString().let·{ %T.valueOf(it) }" else "asString()?.let·{ %T.valueOf(it) }", typeName.nonnull)
}