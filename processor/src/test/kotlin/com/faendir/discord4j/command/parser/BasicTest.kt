package com.faendir.discord4j.command.parser

import org.junit.jupiter.api.Test

class BasicTest {

    @Test
    fun `usage`() {
        compile(
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
                import strikt.assertions.isNotNull
                fun test() {
                    expectThat(SampleParser.buildData()).isNotNull()
                }
            """
        )
    }
}