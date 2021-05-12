package com.faendir.discord4j.command.test

import strikt.api.expectThat
import strikt.assertions.isEqualTo

fun main() {
    expectThat(SampleParser.buildData()) {
        get { options().get().size }.isEqualTo(5)
    }
}