plugins { id("movieapp.kmp.library") }

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(project(":core:common"))
            api(project(":core:domain"))
            implementation(lib("paging-common"))
            implementation(lib("kotlinx-coroutines-test"))
        }
    }
}
