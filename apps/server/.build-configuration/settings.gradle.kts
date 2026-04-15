dependencyResolutionManagement {
    versionCatalogs.create("libs").from(files("../versions.toml"))
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "build-configuration"
