package com.faendir.discord4j.command.parser.parameter

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.CodeBlock
import discord4j.rest.util.ApplicationCommandOptionType
import io.github.enjoydambience.kotlinbard.codeBlock

class IntParameter(parameter: KSValueParameter, index: Int) : Parameter(parameter, index) {
    override val optionType: ApplicationCommandOptionType = ApplicationCommandOptionType.INTEGER
    val typeDeclaration = type.declaration as KSClassDeclaration

    override fun convertValue(): CodeBlock = codeBlock(if (isRequired) "asLong().toInt()" else "asLong()?.toInt()")
}