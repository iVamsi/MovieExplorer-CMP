plugins { id("movieapp.android.application") }

dependencies {
    implementation(project(":composeApp"))
    implementation(lib("androidx-activity-compose"))
    implementation(lib("androidx-appcompat"))
    implementation(lib("androidx-biometric"))
    implementation(platform(lib("koin-bom")))
    implementation(lib("koin-android"))
    debugImplementation(lib("compose-ui-tooling"))
}
