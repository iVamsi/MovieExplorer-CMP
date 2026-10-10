plugins { id("movieapp.kmp.library") }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:common"))
            implementation(lib("ktor-client-core"))
            implementation(lib("ktor-client-contentNegotiation"))
            implementation(lib("ktor-serialization-json"))
            implementation(lib("ktor-client-logging"))
        }
        androidMain.dependencies {
            implementation(lib("ktor-client-okhttp"))
        }
        iosMain.dependencies {
            implementation(lib("ktor-client-darwin"))
        }
        commonTest.dependencies {
            implementation(lib("ktor-client-mock"))
        }
    }
}
