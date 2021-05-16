package com.faendir.discord4j.command.parser

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSClassifierReference
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Light check without type resolution. A positive result does not guarantee equality
 *
 * @return false if this is not equal to annotation
 */
private fun <T : Annotation> KSAnnotation.couldBe(annotation: KClass<T>): Boolean {
    val classifierRef = annotationType.element as? KSClassifierReference
    return classifierRef == null || classifierRef.referencedName() == annotation.simpleName
}


/**
 * Heavy check with type resolution
 *
 * @return true if this is equal to annotation
 */
private fun <T : Annotation> KSAnnotation.isEqualTo(annotation: KClass<T>) =
    annotationType.resolve().declaration.qualifiedName?.asString() == annotation.java.name

inline fun <reified T : Annotation> KSAnnotated.hasAnnotation() = hasAnnotation(T::class)

fun <T : Annotation> KSAnnotated.hasAnnotation(annotation: KClass<T>): Boolean {
    return annotations.filter { it.couldBe(annotation) }.any { it.isEqualTo(annotation) }
}

inline fun <reified T : Annotation> KSAnnotated.findAnnotation() = findAnnotation(T::class)

fun <T : Annotation> KSAnnotated.findAnnotation(annotation: KClass<T>): KSAnnotation? {
    return annotations.filter { it.couldBe(annotation) }.firstOrNull { it.isEqualTo(annotation) }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Annotation, R> KSAnnotated.findAnnotationProperty(property: KProperty1<T, R>): R? {
    return findAnnotation<T>()?.arguments?.firstOrNull { it.name?.asString() == property.name }?.value as R?
}

inline fun <reified T : Annotation> KSAnnotated.findAnnotationTypeProperty(property: KProperty1<T, KClass<*>>): KSType? {
    return findAnnotation<T>()?.arguments?.firstOrNull { it.name?.asString() == property.name }?.value as? KSType?
}

fun Resolver.getClassesWithAnnotation(name: String) = getSymbolsWithAnnotation(name).filterIsInstance<KSClassDeclaration>()

val KSDeclaration.normalizedPackageName
    get() = packageName.asString().takeIf { it != "<root>" } ?: ""