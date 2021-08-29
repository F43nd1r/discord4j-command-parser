package com.faendir.discord4j.command.parser.parameter

import com.faendir.discord4j.command.parser.asTypeName
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.CodeBlock
import discord4j.rest.util.ApplicationCommandOptionType
import io.github.enjoydambience.kotlinbard.CodeBlockBuilder
import io.github.enjoydambience.kotlinbard.codeBlock

class CustomParameter(parameter: KSValueParameter, index: Int, private val converter: KSType) : Parameter(parameter, index) {
    override val optionType: ApplicationCommandOptionType = ApplicationCommandOptionType.STRING
    override fun convertValue(): CodeBlock = codeBlock("let·{·value·-> %T().fromString(event, value.asString()) }", converter.asTypeName())
}