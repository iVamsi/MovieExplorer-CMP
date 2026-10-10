import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("movieapp.detekt")
}

kotlin {
    jvmToolchain(21)

    android {
        namespace = "com.movieapp." + project.path.removePrefix(":").replace(":", ".").replace("-", "").lowercase()
        compileSdk = catalogVersion("compileSdk").toInt()
        minSdk = catalogVersion("minSdk").toInt()
        withHostTest { }
        compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
    }
    iosArm64()
    iosSimulatorArm64()

    compilerOptions {
        optIn.addAll(
            "kotlin.time.ExperimentalTime",
            "kotlin.uuid.ExperimentalUuidApi",
            "kotlinx.coroutines.ExperimentalCoroutinesApi",
        )
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project.dependencies.platform(lib("koin-bom")))
            implementation(lib("koin-core"))
            implementation(lib("kotlinx-coroutines-core"))
            implementation(lib("kotlinx-serialization-json"))
            implementation(lib("kermit"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(lib("kotlinx-coroutines-test"))
            implementation(lib("turbine"))
        }
        getByName("androidHostTest").dependencies {
            implementation(lib("junit"))
            implementation(lib("mockk"))
            implementation(lib("truth"))
        }
    }
}
