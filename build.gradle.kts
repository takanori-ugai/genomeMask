import io.gitlab.arturbosch.detekt.Detekt
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.serialization") version "1.9.20"
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
    jacoco
    id("org.jetbrains.dokka") version "1.9.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.1"
//    id("com.github.sherter.google-java-format") version "0.9"
//    kotlin("jupyter.api") version "0.10.1-8"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.0"
    id("com.github.jk1.dependency-license-report") version "2.5"
    id("com.github.spotbugs") version "5.1.3"
    id("com.diffplug.spotless") version "6.21.0"
    application
}

group = "jp.live.ugai"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.jena:apache-jena-libs:4.10.0")
    testImplementation(kotlin("test"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }

    compileJava {
        options.encoding = "UTF-8"
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    compileTestJava {
        options.encoding = "UTF-8"
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    test {
        useJUnitPlatform()
        finalizedBy(jacocoTestReport) // report is always generated after tests run
    }

    withType<Detekt>().configureEach {
        // Target version of the generated JVM bytecode. It is used for type resolution.
        jvmTarget = "11"
        reports {
            // observe findings in your browser with structure and code snippets
            html.required.set(true)
            // checkstyle like format mainly for integrations like Jenkins
            xml.required.set(true)
            // similar to the console output, contains issue signature to manually edit baseline files
            txt.required.set(true)
            // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations
            // with Github Code Scanning
            sarif.required.set(true)
        }
    }

    jacocoTestReport {
        dependsOn(test) // tests are required to run before generating the report
    }
}

application {
    mainClass.set("jp.live.ugai.genomemask.MainKt")
}

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    coloredOutput.set(true)
    reporters {
        reporter(ReporterType.CHECKSTYLE)
        reporter(ReporterType.JSON)
        reporter(ReporterType.HTML)
    }
    filter {
        exclude("**/style-violations.kt")
    }
}

detekt {
    buildUponDefaultConfig = true // preconfigure defaults
    allRules = false // activate all available (even unstable) rules.
    config.setFrom("$projectDir/config/detekt.yml")
}

spotbugs {
    ignoreFailures.set(true)
}
