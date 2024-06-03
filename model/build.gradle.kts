import java.time.LocalDateTime
import java.io.PrintWriter

plugins {
    `java-library`
    id("io.freefair.lombok") version "8.6"
}

version = "0.0.3-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-core:2.16.2")
    api("com.fasterxml.jackson.core:jackson-databind:2.16.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.16.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.2")
    implementation("com.fasterxml.jackson.module:jackson-module-android-record:2.16.2");
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

tasks.whenTaskAdded {
    // this could be a one liner but IntelliJ does not like that
    val timePath = project.projectDir.absolutePath + "/src/main/resources/generated/compilation-time"
    val timeWriter = PrintWriter(File(timePath))
    timeWriter.println(LocalDateTime.now().withNano(0))
    timeWriter.flush()
    timeWriter.close()

    val commitPath = project.projectDir.absolutePath + "/src/main/resources/generated/commit-id"
    val commitWriter = PrintWriter(File(commitPath))
    commitWriter.println(
        Runtime.getRuntime().exec(arrayOf("git", "describe", "--always", "--dirty")).inputReader().readLine()
    )
    commitWriter.flush()
    commitWriter.close()
}
