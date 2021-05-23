package com.faendir.discord4j.command.parser

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.java
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.intellij.lang.annotations.Language
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.File

internal val KotlinCompilation.Result.workingDir: File
    get() = outputDirectory.parentFile!!

val KotlinCompilation.Result.kspGeneratedSources: List<SourceFile>
    get() = workingDir.resolve("ksp/sources/kotlin").collectSourceFiles()

private fun File.collectSourceFiles(): List<SourceFile> {
    return walkTopDown().filter {
        it.isFile
    }.map { file ->
        SourceFile.fromPath(file)
    }.toList()
}

fun compileKotlin(@Language("kotlin") source: String, @Language("kotlin") eval: String): KotlinCompilation.Result {
    return compile(listOf(kotlin("Source.kt", source)), kotlin("Eval.kt", eval))
}

fun compileJava(vararg sources: SourceFile, @Language("kotlin") eval: String): KotlinCompilation.Result {
    return compile(sources.toList() + kotlin("Dummy.kt", ""), kotlin("Eval.kt", eval))
}

private fun compile(
    src: List<SourceFile>,
    evl: SourceFile
): KotlinCompilation.Result {
    val compilation = KotlinCompilation().apply {
        inheritClassPath = true
        jvmTarget = "1.8"
        sources = src
        symbolProcessorProviders = listOf(ProcessorProvider())
    }
    val pass1 = compilation.compile()
    expectThat(pass1).get(KotlinCompilation.Result::exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    val pass2 = KotlinCompilation().apply {
        inheritClassPath = true
        jvmTarget = "1.8"
        sources = compilation.sources + pass1.kspGeneratedSources + evl
    }.compile()
    expectThat(pass2).get(KotlinCompilation.Result::exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    pass2.classLoader.loadClass("EvalKt").declaredMethods[0].invoke(null)
    return pass2
}
