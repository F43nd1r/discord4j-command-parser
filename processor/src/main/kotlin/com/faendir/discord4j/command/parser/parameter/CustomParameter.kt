package com.faendir.discord4j.command.parser.parameter

import com.faendir.discord4j.command.parser.asTypeName
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.CodeBlock
import discord4j.core.`object`.command.ApplicationCommandOption
import io.github.enjoydambience.kotlinbard.codeBlock

class CustomParameter(parameter: KSValueParameter, index: Int, private val converter: KSType) : Parameter(parameter, index) {
    override val optionType: ApplicationCommandOption.Type = ApplicationCommandOption.Type.STRING
    override fun convertValue(): CodeBlock = codeBlock("let·{·value·-> %T().fromString(event, value.asString()) }", converter.asTypeName())
}