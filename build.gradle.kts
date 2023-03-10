val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val exposed_version : String by project
val koin_ktor: String by project
val h2_version : String by project
val koin_version: String by project
val postgresql: String by project
val kmongo: String by project

plugins {

    kotlin("jvm") version "1.8.10"
    id("io.ktor.plugin") version "2.2.4"
                id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"

}

group = "com.silverbullet"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {

    // Ktor Core
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")

    // Ktor Engine
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")

    // Ktor Auth
    implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktor_version")

    // Ktor Websockets
    implementation("io.ktor:ktor-server-websockets-jvm:$ktor_version")

    // Ktor Json
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")

    // Ktor Default Headers
    implementation("io.ktor:ktor-server-default-headers-jvm:$ktor_version")

    // Ktor Status Pages
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // Ktor Logging
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")

    // Ktor Utils ( not sure what this dep is used for yet)
    implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")

    // Exposed
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")

    // HikariCP
    implementation("com.zaxxer:HikariCP:5.0.1")

    // Postgres
    implementation("org.postgresql:postgresql:$postgresql")

    // KMONGO
    implementation("org.litote.kmongo:kmongo:$kmongo")
    implementation("org.litote.kmongo:kmongo-coroutine:$kmongo")

    // Jbcrypt
    implementation("org.mindrot:jbcrypt:0.4")

    // Koin for Ktor
    implementation("io.insert-koin:koin-ktor:$koin_ktor")

    // SLF4J Logger
    implementation("io.insert-koin:koin-logger-slf4j:$koin_ktor")

    // Ktor Testing
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")

    // Koin Test features
    testImplementation("io.insert-koin:koin-test:$koin_version")

    // Koin for JUnit 4
    testImplementation("io.insert-koin:koin-test-junit4:$koin_version")

    // Kotlin Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}