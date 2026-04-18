package extensions

import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import javax.inject.Inject

abstract class BuildProcessExtension {

    @get:Inject
    protected abstract val objects: ObjectFactory

    val features: Features = objects.newInstance(Features::class.java)
    val versions : Versions = objects.newInstance(Versions::class.java)
    val docker : Docker = objects.newInstance(Docker::class.java)

    /**
     * Configures the versions properties.
     *
     * Inside this function, the receiver is a [Versions] instance,
     * so you have access to all its properties.
     *
     * Configurable Properties:
     *
     * - `javaLanguage: Property<String>`
     * - `springBoot: Property<String>`
     * - `springCloud: Property<String>`
     * - `springOpenApi: Property<String>`
     *
     */
    fun versions(configAction: Versions.() -> Unit) {
        configAction(versions)
    }

    /**
     * Configures the features properties.
     *
     * Inside this function, the receiver is a [Features] instance,
     * so you have access to all its properties.
     *
     * Configurable Properties:
     *
     * - `applicationType: Property<`[ApplicationType]`>`
     * - `dbTechnology: Property<`[DatabaseTechnology]`>`
     * - `springCloudEnabled: Property<Boolean>`
     * - `commonLibs: SetProperty<Provider<MinimalExternalModuleDependency>>`
     * - `commonTestLibs: SetProperty<Provider<MinimalExternalModuleDependency>>`
     *
     */
    fun features(configAction: Features.() -> Unit) {
        configAction(features)
    }

    /**
     * Configures the Docker properties.
     *
     * Inside this function, the receiver is a [Docker] instance,
     * so you have access to all its properties.
     *
     * Configurable Properties:
     *
     * - `appName: Property<String>`
     * - `baseDockerImageLocation: Property<String>`
     * - `baseDockerImageVersion: Property<String>`
     *
     */
    fun docker(configAction: Docker.() -> Unit) {
        configAction(docker)
    }
}


/**
 * The [ApplicationType] enum represents the type of application to be created.
 *
 * The enum contains the following types:
 * - `CLI` for Command Line Interface applications.
 * - `WEB` for Web-based applications. If this type is active, the module will include `spring-boot-web` by default.
 * - `CUSTOM` for customized applications with specific features beyond the standard CLI and WEB options.
 *
 */
enum class ApplicationType {
    CLI,
    WEB,
    CUSTOM
}




/**
 * The [TestFrameworkType] enum represents the type of the test framework that should be configured.
 *
 * The enum contains the following types:
 * - `JUnit5` for applications using JUnit 5.
 * - `JUnit4` for applications using JUnit 4.
 *
 */
enum class TestFrameworkType {
    JUnit5,
    JUnit4
}

enum class DatabaseTechnology {
    MONGO,
    POSTGRES,
}

interface Features {
    val applicationType: Property<ApplicationType>
    val dbTechnology: Property<DatabaseTechnology>
    val springCloudEnabled: Property<Boolean>
    val testFramework: Property<TestFrameworkType>
    /**
     * Everything defined in here is included into the main application module 'id("executable")'.
     * This should be used to incorporate our util-starters into the spring context.
     */
    val starters: SetProperty<Provider<MinimalExternalModuleDependency>>

    /**
     * Common Libs are included into **EVERY MODULE** of the application so the respective interfaces and classes can be used everywhere
     */
    val commonLibs: SetProperty<Provider<MinimalExternalModuleDependency>>
    val commonTestLibs: SetProperty<Provider<MinimalExternalModuleDependency>>
    val annotationProcessors: SetProperty<Provider<MinimalExternalModuleDependency>>
}

interface Docker {
    val appName: Property<String>
    val baseDockerImageLocation: Property<String>
    val baseDockerImageVersion: Property<String>
}


interface Versions {

    // Project Versions
    val javaLanguage: Property<String>


    // StdLib
    val stdlib: Property<String>


    // Spring Versions
    val springBoot: Property<String>
    val springCloud: Property<String>
    val springOpenApi: Property<String>

}
