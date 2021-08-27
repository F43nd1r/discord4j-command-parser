package com.faendir.discord4j.command.annotation

import discord4j.core.event.domain.interaction.SlashCommandEvent
import reactor.core.publisher.Mono
import java.util.*
import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Converter(val value: KClass<out OptionConverter<*>>)

interface OptionConverter<T> {
    fun fromString(context: SlashCommandEvent, string: String?): Mono<Optional<T>>
}
