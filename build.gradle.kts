plugins {
    `java-library`
    signing
    `maven-publish`
    id("me.champeau.jmh") version "0.6.6"
}

group = "com.github.pcimcioch"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
    withSourcesJar()
}

dependencies {
    testImplementation("org.assertj:assertj-core:3.22.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("Memory Store")
                description.set("Library helping to efficiently store structured data in memory")
                url.set("https://github.com/pcimcioch/memory-store")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/pcimcioch/memory-store.git")
                    developerConnection.set("scm:git:git@github.com:pcimcioch/memory-store.git")
                    url.set("https://github.com/pcimcioch/memory-store")
                }
                developers {
                    developer {
                        id.set("pcimcioch")
                        name.set("Przemysław Cimcioch")
                        email.set("cimcioch.przemyslaw@gmail.com")
                    }
                }
            }
        }
    }

    repositories {
        val url = if (project.version.toString().contains("SNAPSHOT")) "https://oss.sonatype.org/content/repositories/snapshots" else "https://oss.sonatype.org/service/local/staging/deploy/maven2"
        maven(url) {
            credentials {
                username = project.findProperty("ossrh.username")?.toString() ?: ""
                password = project.findProperty("ossrh.password")?.toString() ?: ""
            }
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}