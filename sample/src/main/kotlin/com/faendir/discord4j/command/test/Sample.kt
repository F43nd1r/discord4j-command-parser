package com.faendir.discord4j.command.test

import com.faendir.discord4j.command.annotation.ApplicationCommand
import com.faendir.discord4j.command.annotation.Converter
import com.faendir.discord4j.command.annotation.OptionConverter
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent

@ApplicationCommand
class Sample(val name: String, val age: Int, val male: Boolean?, val companyName: Company, @Converter(SuitcaseConverter::class) val suitcase: Suitcase?)

@ApplicationCommand
class Sample2()

@ApplicationCommand
class Sample3(val name: String)

enum class Company {
    A, B, X
}

class Suitcase(vararg val contents: String)

class SuitcaseConverter : OptionConverter<Suitcase> {

    override fun fromString(context: ChatInputInteractionEvent, string: String): Suitcase {
        return Suitcase(*string.split(", ").toTypedArray())
    }
}