package extensions

import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.api.provider.Property


val Project.config: DericonExtension
    get() = rootProject.extensions.getByType<DericonExtension>()

fun <T : Any> Property<T>.ifPresent(action: (T) -> Unit) {
    if (isPresent) {
        action(get())
    }
}
