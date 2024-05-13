plugins {
    java
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    id("io.freefair.lombok") version "8.6"
}

version = "0.0.5-SNAPSHOT"

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

    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

    implementation("org.locationtech.jts:jts-core:1.19.0")
    implementation("com.auth0:java-jwt:2.0.1")

    compileOnly("org.springframework.boot:spring-boot-devtools")

    runtimeOnly("com.h2database:h2:2.2.224")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.3")
}
