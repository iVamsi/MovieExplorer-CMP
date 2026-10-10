plugins { id("io.gitlab.arturbosch.detekt") }

detekt {
    buildUponDefaultConfig = true
    parallel = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    source.setFrom(
        listOf("commonMain", "androidMain", "iosMain", "commonTest", "androidHostTest", "main")
            .map { file("src/$it/kotlin") }
            .filter { it.exists() },
    )
}

dependencies { detektPlugins(lib("detekt-formatting")) }
