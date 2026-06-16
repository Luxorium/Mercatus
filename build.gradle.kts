plugins {
    java
}

group = "dev.luxorium"
version = "0.1.0"
description = "Folia-native player shops and trading for Paper/Folia 26.1.2 Minecraft servers."

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    testCompileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    implementation("org.xerial:sqlite-jdbc:3.53.2.0")

    testImplementation(platform("org.junit:junit-bom:5.13.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.jar {
    archiveBaseName.set("Mercatus")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith(".jar") }
            .map { zipTree(it) }
    })
}

tasks.test {
    useJUnitPlatform()
}
