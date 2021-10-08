package com.faendir.discord4j.command.parser.parameter

import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.CodeBlock
import discord4j.core.`object`.command.ApplicationCommandOption
import io.github.enjoydambience.kotlinbard.codeBlock

class IntParameter(parameter: KSValueParameter, index: Int) : Parameter(parameter, index) {
    override val optionType: ApplicationCommandOption.Type = ApplicationCommandOption.Type.INTEGER

    override fun convertValue(): CodeBlock = codeBlock(if (isRequired) "asLong().toInt()" else "asLong()?.toInt()")
}