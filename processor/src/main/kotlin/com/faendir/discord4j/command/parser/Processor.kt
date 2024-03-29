package com.faendir.discord4j.command.parser

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated

class Processor(private val kotlinVersion: KotlinVersion, private val out: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> = Generator(logger, out, resolver).process()
}