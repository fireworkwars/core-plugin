import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.1.0"
    id("io.papermc.paperweight.userdev") version "1.7.3"
    id("com.gradleup.shadow") version "8.3.5"
    id("maven-publish")
}

group = "foundation.esoteric"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks.named("shadowJar").get()) {
                classifier = null // Ensures it replaces the default JAR
            }
            groupId = "foundation.esoteric"
            version = "1.0-SNAPSHOT"
        }
    }

    repositories {
        maven {
            name = "local"
            url = uri("file://${rootProject.rootDir}/local-maven-repo")
        }
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    paperweight.paperDevBundle("1.21.3-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.apache.commons", "commons-compress", "1.24.0")
}

val targetJavaVersion = 21

kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks {
    shadowJar {
        minimize {
            exclude(dependency("org.jetbrains.kotlin:kotlin-reflect"))
        }
    }

    build {
        dependsOn(shadowJar)
    }

    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }

    compileKotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }
}
