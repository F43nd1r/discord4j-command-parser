package com.faendir.discord4j.command.parser

import com.faendir.discord4j.command.annotation.ApplicationCommand
import com.faendir.discord4j.command.annotation.ApplicationCommandConstructor
import com.faendir.discord4j.command.parser.parameter.Parameter
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import discord4j.rest.util.ApplicationCommandOptionType
import io.github.enjoydambience.kotlinbard.addCode
import io.github.enjoydambience.kotlinbard.addFunction
import io.github.enjoydambience.kotlinbard.addObject
import io.github.enjoydambience.kotlinbard.buildFile
import net.pearx.kasechange.toKebabCase

const val DEFAULTS_BITFLAGS_FIELD_NAME = "_defaultsBitFlags"

class DslGenerator(
    private val kotlinVersion: KotlinVersion,
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
    private val resolver: Resolver
) {
    private val enumType = resolver.getClassDeclarationByName<Enum<*>>()!!.asStarProjectedType()

    private fun error(reason: String, clazz: KSClassDeclaration) {
        logger.error("@AutoDsl can't be applied to $clazz: $reason", clazz)
    }


    fun process(): List<KSAnnotated> {
        return resolver.getClassesWithAnnotation(ApplicationCommand::class.java.name).filterNot { generate(it) }
    }


    /**
     * returns true if class generation was successful
     */
    private fun generate(clazz: KSClassDeclaration): Boolean {
        if (clazz.isAbstract()) {
            error("must not be abstract", clazz)
            return false
        }
        val constructors = clazz.getConstructors().filter { it.isPublic() || it.isInternal() }
        if (constructors.isEmpty()) {
            error("must have at least one public or internal constructor", clazz)
            return false
        }
        val constructor = constructors.firstOrNull { it.hasAnnotation<ApplicationCommandConstructor>() }
            ?: clazz.primaryConstructor
            ?: constructors.first()
        if (!constructor.validate()) {
            //defer processing
            return false
        }
        val parameters = constructor.parameters.mapIndexed { index, parameter -> Parameter(parameter, index) }
        val type = clazz.asStarProjectedType().asClassName()
        val subCommand = clazz.findAnnotationProperty(ApplicationCommand::subCommand) ?: false
        val dataType = if (subCommand) ApplicationCommandOptionData::class else ApplicationCommandRequest::class
        val parserType = type.withParserSuffix()
        buildFile(parserType.packageName, parserType.simpleName) {
            addObject(parserType) {
                addFunction("buildData") {
                    returns(dataType)
                    addCode {
                        add("return %T.builder()\n", dataType)
                        indent()
                        val name = clazz.findAnnotationProperty(ApplicationCommand::name)?.takeIf { it.isNotEmpty() } ?: type.simpleName.toKebabCase()
                        add(".name(%S)\n", name)
                        add(".description(%S)\n", clazz.findAnnotationProperty(ApplicationCommand::description)?.takeIf { it.isNotEmpty() } ?: name)
                        if (subCommand) add(".type(%T.SUB_COMMAND.value)\n", ApplicationCommandOptionType::class)
                        for (parameter in parameters.sortedByDescending { it.isRequired }) {
                            add(".addOption(%L)\n", parameter.buildData())
                        }
                        add(".build()")
                        unindent()
                    }
                }
                addFunction("parse") {
                    returns(type)
                    addParameter("options", List::class.parameterizedBy(ApplicationCommandInteractionOption::class))
                    addCode {
                        add("return %T(\n", type)
                        indent()
                        for(parameter in parameters) {
                            add("%L, \n", parameter.passToConstructor())
                        }
                        unindent()
                        add(")")
                    }
                }
            }
        }.writeTo(clazz.containingFile!!, codeGenerator)
        return true
    }

}