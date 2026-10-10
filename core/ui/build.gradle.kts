plugins { id("movieapp.kmp.compose") }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:common"))
            implementation(lib("paging-compose"))
        }
        androidMain.dependencies {
            implementation(lib("koin-android"))
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.movieapp.core.ui.resources"
}
