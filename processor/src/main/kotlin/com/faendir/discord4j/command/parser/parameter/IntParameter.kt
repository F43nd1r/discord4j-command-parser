package com.faendir.discord4j.command.parser.parameter

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import discord4j.rest.util.ApplicationCommandOptionType
import io.github.enjoydambience.kotlinbard.CodeBlockBuilder
import io.github.enjoydambience.kotlinbard.codeBlock

class IntParameter(parameter: KSValueParameter, index: Int) : Parameter(parameter, index) {
    override val optionType: ApplicationCommandOptionType = ApplicationCommandOptionType.INTEGER

    override fun CodeBlockBuilder.mapToOptional()  = mapToOptional(codeBlock(if (isRequired) "asLong().toInt()" else "asLong()?.toInt()"))
}