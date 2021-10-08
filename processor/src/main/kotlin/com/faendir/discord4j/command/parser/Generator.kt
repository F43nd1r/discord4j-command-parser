package com.faendir.discord4j.command.parser

import com.faendir.discord4j.command.annotation.ApplicationCommand
import com.faendir.discord4j.command.annotation.ApplicationCommandConstructor
import com.faendir.discord4j.command.parser.parameter.ParameterFactory
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
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import io.github.enjoydambience.kotlinbard.`while`
import io.github.enjoydambience.kotlinbard.addCode
import io.github.enjoydambience.kotlinbard.addFunction
import io.github.enjoydambience.kotlinbard.addObject
import io.github.enjoydambience.kotlinbard.buildFile
import net.pearx.kasechange.toKebabCase

class Generator(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
    private val resolver: Resolver
) {

    private fun error(reason: String, clazz: KSClassDeclaration) {
        logger.error("@AutoDsl can't be applied to $clazz: $reason", clazz)
    }


    fun process(): List<KSAnnotated> {
        return resolver.getClassesWithAnnotation(ApplicationCommand::class.java.name).filterNot { generate(it) }.toList()
    }


    /**
     * returns true if class generation was successful
     */
    private fun generate(clazz: KSClassDeclaration): Boolean {
        if (clazz.isAbstract()) {
            error("must not be abstract", clazz)
            return false
        }
        val constructors = clazz.getConstructors().filter { it.isPublic() || it.isInternal() }.toList()
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
        val parameters = ParameterFactory.createParameters(constructor.parameters, logger)
        val type = clazz.asStarProjectedType().asClassName()
        val subCommand = clazz.findAnnotationProperty(ApplicationCommand::subCommand) ?: false
        val dataType = if (subCommand) ApplicationCommandOptionData::class else ApplicationCommandRequest::class
        val parserType = type.withParserSuffix()
        buildFile(parserType.packageName, parserType.simpleName) {
            addObject(parserType) {
                addFunction("buildData") {
                    addAnnotation(JvmStatic::class)
                    returns(dataType)
                    addCode {
                        add("return %T.builder()\n", dataType)
                        indent()
                        val name = clazz.findAnnotationProperty(ApplicationCommand::name)?.takeIf { it.isNotEmpty() } ?: type.simpleName.toKebabCase()
                        add(".name(%S)\n", name)
                        add(".description(%S)\n", clazz.findAnnotationProperty(ApplicationCommand::description)?.takeIf { it.isNotEmpty() } ?: name)
                        if (subCommand) add(".type(%T.Type.SUB_COMMAND.value)\n", ApplicationCommandOption::class)
                        for (parameter in parameters.sortedByDescending { it.isRequired }) {
                            add(".addOption(%L)\n", parameter.buildData())
                        }
                        add(".build()")
                        unindent()
                    }
                }
                addFunction("parse") {
                    addAnnotation(JvmStatic::class)
                    returns(type)
                    addParameter("event", ChatInputInteractionEvent::class)
                    addCode {
                        if (parameters.isEmpty()) {
                            add("return %T()", type)
                        } else {
                            addStatement("var options = event.options")
                            addStatement("var option = options.first()")
                            `while`("option.type == %1T.Type.SUB_COMMAND || option.type == %1T.Type.SUB_COMMAND_GROUP", ApplicationCommandOption::class) {
                                addStatement("options = option.options")
                                addStatement("option = options.first()")
                            }
                            add("return %T(\n", type)
                            indent()
                                for (parameter in parameters) {
                                    add("%L,\n", parameter.toMonoBlock())
                                }
                            unindent()
                            add(")\n")
                        }
                    }
                }
            }
        }.writeTo(clazz.containingFile!!, codeGenerator)
        return true
    }

}