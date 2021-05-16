package com.faendir.discord4j.command.parser.parameter

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.CodeBlock
import discord4j.rest.util.ApplicationCommandOptionType
import io.github.enjoydambience.kotlinbard.codeBlock

class BooleanParameter(parameter: KSValueParameter, index: Int) : Parameter(parameter, index) {
    override val optionType: ApplicationCommandOptionType = ApplicationCommandOptionType.BOOLEAN

    override fun convertValue(): CodeBlock = codeBlock("asBoolean()")
}