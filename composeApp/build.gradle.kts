import java.util.Properties

plugins { id("movieapp.kmp.compose") }

val generateAppConfig = tasks.register("generateAppConfig") {
    val props = Properties().apply {
        rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
    }
    val apiKey = props.getProperty("TMDB_API_KEY") ?: System.getenv("TMDB_API_KEY").orEmpty()
    val baseUrl = props.getProperty("TMDB_BASE_URL") ?: "https://api.themoviedb.org/3/"
    val logging = props.getProperty("NETWORK_LOGGING")?.toBoolean() ?: false
    val outDir = layout.buildDirectory.dir("generated/appConfig/kotlin")
    inputs.property("apiKey", apiKey)
    inputs.property("baseUrl", baseUrl)
    inputs.property("logging", logging)
    outputs.dir(outDir)
    doLast {
        val file = outDir.get().file("com/movieapp/config/AppConfig.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            |package com.movieapp.config
            |
            |internal object AppConfig {
            |    const val TMDB_API_KEY: String = "$apiKey"
            |    const val TMDB_BASE_URL: String = "$baseUrl"
            |    const val NETWORK_LOGGING: Boolean = $logging
            |    const val VERSION_NAME: String = "1.0.0"
            |}
            |""".trimMargin(),
        )
    }
}

kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:model"))
            implementation(project(":core:network"))
            implementation(project(":core:database"))
            implementation(project(":core:domain"))
            implementation(project(":core:ui"))
            implementation(project(":data:actors"))
            implementation(project(":data:movies"))
            implementation(project(":data:series"))
            implementation(project(":data:user"))
            implementation(project(":feature:actors"))
            implementation(project(":feature:collections"))
            implementation(project(":feature:movies"))
            implementation(project(":feature:profile"))
            implementation(project(":feature:search"))
            implementation(project(":feature:series"))
            implementation(project(":feature:settings"))
            implementation(lib("navigation-compose"))
            implementation(lib("compose-material3-navigationSuite"))
            implementation(lib("compose-material3-adaptive"))
            implementation(lib("compose-material3-adaptive-layout"))
            implementation(lib("compose-material3-adaptive-navigation"))
            implementation(lib("coil-network-ktor3"))
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(lib("datastore-preferences"))
            implementation(lib("kotlinx-coroutines-test"))
        }
        commonMain { kotlin.srcDir(generateAppConfig) }
        androidMain.dependencies {
            implementation(lib("androidx-appcompat"))
            implementation(lib("koin-android"))
        }
    }
}

compose.resources { packageOfResClass = "com.movieapp.app.resources" }
