plugins { id("movieapp.kmp.library") }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(lib("datastore-preferences"))
            implementation(lib("kotlinx-datetime"))
        }
        androidMain.dependencies {
            implementation(lib("kotlinx-coroutines-android"))
            implementation(lib("koin-android"))
        }
    }
}
