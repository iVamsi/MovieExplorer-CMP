plugins { id("movieapp.kmp.compose") }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:common"))
            implementation(project(":core:domain"))
            implementation(project(":core:ui"))
            implementation(lib("paging-common"))
            implementation(lib("paging-compose"))
        }
        commonTest.dependencies { implementation(project(":core:testing")) }
        getByName("androidHostTest").dependencies { implementation(project(":core:testing")) }
    }
}
