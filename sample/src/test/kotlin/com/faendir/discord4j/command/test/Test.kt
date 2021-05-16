package com.faendir.discord4j.command.test

import discord4j.discordjson.json.ApplicationCommandOptionData
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo

fun main() {
    expectThat(SampleParser.buildData()) {
        get { options().get().size }.isEqualTo(5)
    }
    expectThat(JavaSampleParser.buildData()) {
        isA<ApplicationCommandOptionData>()
        get { options().get().size }.isEqualTo(4)
        get { name() }.isEqualTo("Sam")
    }

}