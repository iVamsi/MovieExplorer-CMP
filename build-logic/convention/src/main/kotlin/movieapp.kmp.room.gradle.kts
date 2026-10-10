plugins {
    id("movieapp.kmp.library")
    id("com.google.devtools.ksp")
    id("androidx.room")
}

room { schemaDirectory("$projectDir/schemas") }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(lib("room-runtime"))
            implementation(lib("room-paging"))
            implementation(lib("sqlite-bundled"))
        }
    }
}

dependencies {
    listOf("kspAndroid", "kspIosArm64", "kspIosSimulatorArm64").forEach { add(it, lib("room-compiler")) }
}
