pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.jetbrains.kotlinx.kover.aggregation") version "0.9.11"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MovieExplorer-CMP"

include(":androidApp", ":composeApp")
include(":core:common", ":core:model", ":core:network", ":core:database", ":core:domain", ":core:testing", ":core:ui")
include(":data:actors", ":data:movies", ":data:series", ":data:user")
include(
    ":feature:actors", ":feature:collections", ":feature:movies", ":feature:profile",
    ":feature:search", ":feature:series", ":feature:settings",
)

kover {
    enableCoverage()
    reports {
        excludedProjects.addAll(":androidApp", ":core:testing")
        excludedClasses.addAll("*.ComposableSingletons*", "*.*\$*Preview*", "*.generated.resources.*", "com.movieapp.config.*")
        excludesAnnotatedBy.add("androidx.compose.ui.tooling.preview.Preview")
        verify {
            rule { bound { minValue = 14 } }
            rule("domain") {
                filters { includedProjects.set(setOf(":core:domain")) }
                bound { minValue = 80 }
            }
            rule("viewmodels") {
                filters { includedClasses.set(setOf("*ViewModel")) }
                bound { minValue = 80 }
            }
        }
    }
}
