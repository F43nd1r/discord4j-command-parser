package com.faendir.discord4j.command.annotation

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class ApplicationCommand(val name: String = "", val description: String = "", val subCommand: Boolean = false)
