package com.faendir.discord4j.command.parser

import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.Test

class JavaTest {

    @Test
    fun `usage`() {
        compileJava(
            SourceFile.java(
                "Company.java",
                """
                public enum Company {
                    A, B, X
                }
                """
            ),
            SourceFile.java(
            "Sample.java",
            """
                import com.faendir.discord4j.command.annotation.ApplicationCommand;
                import org.jetbrains.annotations.NotNull;
                @ApplicationCommand(name = "Sam", subCommand = true)
                public class Sample {
                    private final String name;
                    private final int age;
                    private final Boolean male;
                    private final Company companyName;
                    
                    public Sample(@NotNull String name, int age, Boolean male, Company companyName) {
                        this.name = name;
                        this.age = age;
                        this.male = male;
                        this.companyName = companyName;
                    }
                }
            """),
            eval = """
                import strikt.api.expectThat
                import strikt.assertions.isEqualTo
                fun test() {
                    expectThat(SampleParser.buildData()) {
                        get { options().get().size }.isEqualTo(4)
                        get { name() }.isEqualTo("Sam")
                    }
                }
            """
        )
    }
}