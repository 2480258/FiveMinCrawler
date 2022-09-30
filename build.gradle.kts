/*
 *
 *     FiveMinCrawler
 *     Copyright (C) 2022  2480258
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("org.panteleyev.jpackageplugin") version "1.3.1"
    jacoco
    application
}

jacoco {
    toolVersion = "0.8.8"
}

tasks.jacocoTestReport {
    reports {
        html.isEnabled = false
        xml.isEnabled = true
        csv.isEnabled = false

        xml.destination = file("./jacocoTestReport.xml")
    }
    
    finalizedBy("jacocoTestCoverageVerification")
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            excludes = listOf("*.integration.*") // exculde integration test
        }
    }
}


val testCoverage by tasks.registering {
    group = "verification"
    description = "Runs the unit tests with coverage"
    
    dependsOn(":test",
        ":jacocoTestReport",
        ":jacocoTestCoverageVerification")
    
    tasks["jacocoTestReport"].mustRunAfter(tasks["test"])
    tasks["jacocoTestCoverageVerification"].mustRunAfter(tasks["jacocoTestReport"])
}

group = "me.azure"
version = "0.1"

var arrow_version = "1.1.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.arrow-kt:arrow-core:$arrow_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation("com.squareup.okhttp3:okhttp-urlconnection:4.9.3")
    implementation("ru.gildor.coroutines:kotlin-coroutines-okhttp:1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.3.2")
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.4")
    implementation("io.github.microutils:kotlin-logging:2.1.21")
    implementation("org.slf4j:slf4j-simple:1.7.32")
    implementation("org.brotli:dec:0.1.2")
    implementation("com.google.guava:guava:31.1-jre")
    
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
    
    implementation("org.pf4j:pf4j:3.7.0")
    
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.12.4")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.6.4")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.test {
    useTestNG()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
}

application {
    mainClass.set("MainKt")
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    languageVersion = "1.6"
}
