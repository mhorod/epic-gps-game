import java.time.LocalDateTime
import java.io.PrintWriter

plugins {
    java
    id("io.freefair.lombok") version "8.6"
}

version = "0.0.3-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.2")
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
