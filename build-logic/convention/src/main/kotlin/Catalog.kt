import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

val Project.catalog: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun Project.lib(alias: String): Provider<MinimalExternalModuleDependency> = catalog.findLibrary(alias).get()

fun Project.catalogVersion(alias: String): String = catalog.findVersion(alias).get().requiredVersion
