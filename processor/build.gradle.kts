plugins {
    kotlin
    `publish-maven`
    testing
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:${properties["kspVersion"]}")
    implementation(project(":annotations"))
    implementation("com.squareup:kotlinpoet:${properties["kotlinPoetVersion"]}")
    implementation("com.faendir:kotlinbard:${properties["kotlinBardVersion"]}")
    implementation("com.discord4j:discord4j-core:${properties["discord4jVersion"]}")
    implementation("net.pearx.kasechange:kasechange-jvm:${properties["kaseChangeVersion"]}")
}