plugins {
    kotlin
    id("com.google.devtools.ksp")
    testing
}

dependencies {
    implementation(project(":annotations"))
    ksp(project(":processor"))
    implementation("com.discord4j:discord4j-core:${properties["discord4jVersion"]}")
}

sourceSets {
    main {
        java {
            srcDir(file("$buildDir/generated/ksp/main/kotlin"))
        }
    }
    test {
        java {
            srcDir(file("$buildDir/generated/ksp/test/kotlin"))
        }
    }
}
