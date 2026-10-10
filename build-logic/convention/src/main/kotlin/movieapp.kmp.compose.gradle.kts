plugins {
    id("movieapp.kmp.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    android { androidResources { enable = true } }
    sourceSets {
        commonMain.dependencies {
            implementation(lib("compose-runtime"))
            implementation(lib("compose-foundation"))
            implementation(lib("compose-ui"))
            implementation(lib("compose-ui-tooling-preview"))
            implementation(lib("compose-components-resources"))
            implementation(lib("compose-material3"))
            implementation(lib("compose-material-icons-extended"))
            implementation(lib("lifecycle-viewmodel-compose"))
            implementation(lib("lifecycle-runtime-compose"))
            implementation(lib("lifecycle-viewmodel-savedstate"))
            implementation(lib("koin-compose"))
            implementation(lib("koin-compose-viewmodel"))
            implementation(lib("coil-compose"))
        }
    }
}
