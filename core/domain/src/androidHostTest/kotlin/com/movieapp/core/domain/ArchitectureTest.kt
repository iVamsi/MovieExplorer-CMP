package com.movieapp.core.domain

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class ArchitectureTest {
    @Test
    fun `core domain must not depend on platform APIs`() {
        Konsist.scopeFromModule("core/domain").files.assertFalse { file ->
            file.hasImport { import ->
                import.name.startsWith("android.") ||
                    import.name.startsWith("androidx.") &&
                        !import.name.startsWith("androidx.paging.")
            }
        }
    }

    @Test
    fun `common source must not import JVM or platform APIs`() {
        Konsist.scopeFromProject().files
            .filter { it.path.replace('\\', '/').contains("/src/commonMain/") }
            .assertFalse { file ->
                file.hasImport { import ->
                    import.name.startsWith("android.") ||
                        import.name.startsWith("java.") ||
                        import.name.startsWith("javax.")
                }
            }
    }

    @Test
    fun `feature modules must not import data layer types`() {
        Konsist.scopeFromProject().files.withPackage("com.movieapp.feature..").assertFalse { file ->
            file.hasImport { import -> import.name.startsWith("com.movieapp.data.") }
        }
    }

    @Test
    fun `feature modules must not import data implementations`() {
        Konsist.scopeFromProject().files.withPackage("com.movieapp.feature..").assertFalse { file ->
            file.hasImport { import ->
                import.name.matches(Regex("com\\.movieapp\\.data\\.[^.]+\\..*Impl$"))
            }
        }
    }

    @Test
    fun `repository interfaces live in core domain`() {
        Konsist.scopeFromProject()
            .interfaces()
            .filter { it.name.endsWith("Repository") }
            .assertTrue { it.resideInPackage("com.movieapp.core.domain.repository..") }
    }
}
