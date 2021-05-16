package com.faendir.discord4j.command.parser

import org.junit.jupiter.api.Test

class BasicTest {

    @Test
    fun `usage`() {
        compileKotlin(
            """
                import com.faendir.discord4j.command.annotation.ApplicationCommand
                @ApplicationCommand
                class Sample(val name: String, val age: Int, val male: Boolean?, val companyName: Company)

                enum class Company{
                    A, B, X
                }
            """,
            """
                import strikt.api.expectThat
                import strikt.assertions.isEqualTo
                fun test() {
                    expectThat(SampleParser.buildData()) {
                        get { options().get().size }.isEqualTo(4)
                    }
                }
            """
        )
    }
}