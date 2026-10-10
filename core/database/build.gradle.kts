plugins { id("movieapp.kmp.room") }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(lib("paging-common"))
        }
        androidMain.dependencies {
            implementation(lib("koin-android"))
        }
    }
}
