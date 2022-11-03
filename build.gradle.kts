import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Versions.kotlinVersion
    `maven-publish`
}

group   = "org.tinygears"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

publishing {
    repositories {
        mavenLocal()
    }

    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])
            groupId    = project.group.toString()
            artifactId = project.name
            version    = project.version.toString()
        }
    }
}