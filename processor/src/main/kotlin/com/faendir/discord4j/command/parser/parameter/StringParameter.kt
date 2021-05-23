package com.faendir.discord4j.command.parser.parameter

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import discord4j.rest.util.ApplicationCommandOptionType
import io.github.enjoydambience.kotlinbard.CodeBlockBuilder
import io.github.enjoydambience.kotlinbard.codeBlock

class StringParameter(parameter: KSValueParameter, index: Int) : Parameter(parameter, index) {
    override val optionType: ApplicationCommandOptionType = ApplicationCommandOptionType.STRING

    override fun CodeBlockBuilder.mapToOptional()  = mapToOptional(codeBlock("asString()"))
}