plugins { id("movieapp.kmp.library") }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:common"))
            implementation(lib("paging-common"))
        }
        getByName("androidHostTest").dependencies {
            implementation(lib("konsist"))
        }
    }
}
