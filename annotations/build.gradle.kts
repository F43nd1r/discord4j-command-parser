plugins {
    kotlin
    `publish-maven`
}

dependencies {
    implementation("com.discord4j:discord4j-core:${properties["discord4jVersion"]}")
}
