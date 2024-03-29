import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    `maven-publish`
    signing
    id("org.jetbrains.dokka")
}

tasks.withType<DokkaTask> {
    dokkaSourceSets.configureEach {
        suppressGeneratedFiles.set(false)
    }
}

val javadocJar = tasks.register<Jar>("javadocJar") {
    group = "documentation"
    from(tasks["dokkaJavadoc"])
    archiveClassifier.set("javadoc")
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
    group = "documentation"
    //from(sourceSets["main"].allSource)
    archiveClassifier.set("sources")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components.findByName("java"))

            artifact(sourcesJar)
            artifact(javadocJar)

            pom {
                name.set("Discord4j Command Parser")
                description.set("Auto-generates helper classes for your discord4j slash commands.")
                url.set("https://github.com/F43nd1r/discord4j-command-parser")
                scm {
                    connection.set("scm:git:https://github.com/F43nd1r/discord4j-command-parser.git")
                    developerConnection.set("scm:git:git@github.com:F43nd1r/discord4j-command-parser.git")
                    url.set("https://github.com/F43nd1r/discord4j-command-parser.git")
                }
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("f43nd1r")
                        name.set("Lukas Morawietz")
                    }
                }
            }
        }
    }
    repositories {
        mavenLocal()
        maven {
            name = "GithubPackages"
            url = uri("https://maven.pkg.github.com/F43nd1r/discord4j-command-parser")
            credentials {
                username = project.findProperty("githubUser") as? String ?: System.getenv("GITHUB_USER")
                password = project.findProperty("githubPackageKey") as? String ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

signing {
    val signingKey = project.findProperty("signingKey") as? String ?: System.getenv("SIGNING_KEY")
    val signingPassword = project.findProperty("signingPassword") as? String ?: System.getenv("SIGNING_PASSWORD")
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["maven"])
}