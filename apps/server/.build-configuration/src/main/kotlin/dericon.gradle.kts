import extensions.ApplicationType.WEB
import extensions.DatabaseTechnology.POSTGRES
import extensions.DericonExtension
import extensions.TestFrameworkType.JUnit5

plugins {
    idea
}

val config = extensions.create<DericonExtension>("dericon")

// Version Conventions
config.versions.javaLanguage.convention("25")

// Feature Conventions
config.features.applicationType.convention(WEB)
config.features.dbTechnology.convention(POSTGRES)
config.features.testFramework.convention(JUnit5)
config.features.starters.convention(emptySet())
config.features.commonLibs.convention(emptySet())
config.features.commonTestLibs.convention(emptySet())
config.features.annotationProcessors.convention(emptySet())

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}