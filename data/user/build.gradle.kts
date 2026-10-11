plugins { id("movieapp.kmp.library") }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:common"))
            implementation(project(":core:domain"))
            implementation(project(":core:network"))
            implementation(project(":core:database"))
            implementation(lib("paging-common"))
            implementation(lib("ktor-client-core"))
            implementation(lib("datastore-preferences"))
            implementation(lib("coil-compose"))
            implementation(lib("multiplatform-settings"))
        }
        androidMain.dependencies {
            implementation(lib("koin-android"))
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(lib("paging-testing"))
        }
        getByName("androidHostTest").dependencies {
            implementation(project(":core:testing"))
            implementation(lib("paging-testing"))
        }
    }
}
