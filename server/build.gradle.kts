plugins {
    java
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    id("io.freefair.lombok") version "8.6"
}

version = "0.0.7-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":model"))

    implementation("org.springframework.boot:spring-boot-starter:3.2.3")
    implementation("org.springframework.boot:spring-boot-starter-web:3.2.3")
    implementation("org.springframework.boot:spring-boot-starter-websocket:3.2.3")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.2.3")
    implementation("org.springframework.boot:spring-boot-starter-security:3.2.5")
    implementation("org.springframework.security:spring-security-messaging:6.3.0")

    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

    implementation("org.locationtech.jts:jts-core:1.19.0")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-security")

    compileOnly("org.springframework.boot:spring-boot-devtools")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    runtimeOnly("com.h2database:h2:2.2.224")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.3")
}
