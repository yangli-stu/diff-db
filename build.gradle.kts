import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

fun prop(key: String): String = providers.gradleProperty(key).get()

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.1.0"
}

group = prop("pluginGroup")
version = prop("pluginVersion")

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    intellijPlatform {
        create(IntelliJPlatformType.fromCode(prop("platformType")), prop("platformVersion"))
        instrumentationTools()
    }

    // Diff engine: bundled into the plugin (/lib).
    implementation("org.liquibase:liquibase-core:${prop("liquibaseVersion")}") {
        exclude(group = "info.picocli")
    }

    // SSH tunnelling (maintained JSch fork).
    implementation("com.github.mwiede:jsch:${prop("jschVersion")}")

    // JDBC drivers bundled so connections work out of the box (~3 MB total).
    // User can still override via "Driver jar" in the connection dialog.
    implementation("com.mysql:mysql-connector-j:${prop("mysqlDriverVersion")}")
    implementation("org.postgresql:postgresql:${prop("postgresqlDriverVersion")}")

    // Tests.
    testImplementation("org.junit.jupiter:junit-jupiter:${prop("junitVersion")}")
    testImplementation("com.h2database:h2:${prop("h2Version")}")
}

intellijPlatform {
    pluginConfiguration {
        id = prop("pluginId")
        name = "DiffDB"
        version = prop("pluginVersion")

        ideaVersion {
            sinceBuild = prop("pluginSinceBuild")
            val until = prop("pluginUntilBuild")
            if (until.isNotBlank()) untilBuild = until else untilBuild = provider { null }
        }

        description = """
            Compare the schema of two databases (MySQL / PostgreSQL), directly or over an SSH tunnel,
            visualize the differences as a tree, and generate dialect-aware migration SQL.
            Works in both Community and Ultimate editions.
        """.trimIndent()
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}
