package com.faendir.discord4j.command.annotation

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.CLASS)
annotation class Name(val value: String)
