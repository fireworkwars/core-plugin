
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml
import xyz.jpenilla.resourcefactory.bukkit.bukkitPluginYaml

plugins {
    kotlin("jvm") version "2.1.0"
    id("io.papermc.paperweight.userdev") version "1.7.3"
    id("xyz.jpenilla.resource-factory-paper-convention") version "1.2.0"
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
    implementation("org.apache.commons:commons-compress:1.26.0")
    implementation("dev.jorel:commandapi-bukkit-shade-mojang-mapped:9.7.0")

    api("fr.mrmicky:fastboard:2.1.3")
}

val targetJavaVersion = 21

kotlin {
    jvmToolchain(targetJavaVersion)

    compilerOptions {
        optIn.add("kotlin.RequiresOptIn")
        freeCompilerArgs.add("-Xjvm-default=all-compatibility")
    }
}

tasks {
    shadowJar {
        minimize {
            exclude(dependency("org.jetbrains.kotlin:kotlin-reflect"))
        }

        relocate("fr.mrmicky.fastboard", "foundation.esoteric.fireworkwarscore.libs.fastboard")
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

paperPluginYaml {
    name = "FireworkWarsCore"
    authors = listOf("rolyPolyVole")
    website = "https://github.com/EsotericFoundation/firework-wars-core-plugin"

    main = "foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin"
    bootstrapper = "foundation.esoteric.fireworkwarscore.FireworkWarsCoreBootstrapper"

    apiVersion = "1.21.3"
    description = project.description
}

bukkitPluginYaml {
    load = BukkitPluginYaml.PluginLoadOrder.STARTUP
}
