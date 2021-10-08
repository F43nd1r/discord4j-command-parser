package com.faendir.discord4j.command.parser.parameter

import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.CodeBlock
import discord4j.core.`object`.command.ApplicationCommandOption
import io.github.enjoydambience.kotlinbard.codeBlock

class StringParameter(parameter: KSValueParameter, index: Int) : Parameter(parameter, index) {
    override val optionType: ApplicationCommandOption.Type = ApplicationCommandOption.Type.STRING
    override fun convertValue(): CodeBlock = codeBlock("asString()")
}