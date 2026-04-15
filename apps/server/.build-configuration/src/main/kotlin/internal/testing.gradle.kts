package internal

import extensions.TestFrameworkType
import extensions.config
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult

plugins {
    id("java")
}

dependencies {

    // Test Framework Configuration
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    config.features.commonTestLibs.get().forEach {
        testImplementation(it)
    }
}

tasks.withType<Test> {

    // Test Framework Configuration
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }

    setMaxParallelForks(Runtime.getRuntime().availableProcessors().div(2))

    // Reporting and logging
    reports {
        html.required.set(true)
    }

    jvmArgs = listOf("-Duser.timezone=Europe/Berlin")
    val profile = providers.systemProperty("test.profile").orElse("unit").get()

    if (profile != "integration") {
        // Integration test naming scheme dictated by the reference architecture
        exclude("**/*TestIT*")
        // Legacy naming schemes
        exclude("**/integration/**")
        exclude("**/e2e/**")
    }

    // Proper logging of test results summary
    addTestListener(
        object : TestListener {
            override fun beforeSuite(suite: TestDescriptor) = Unit

            override fun beforeTest(testDescriptor: TestDescriptor) = Unit

            override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) = Unit

            override fun afterSuite(testDescriptor: TestDescriptor, result: TestResult) {
                if (testDescriptor.parent != null) {
                    return
                }

                val resultsTitle = "Module ${project.name} (profile = ${profile}) test results:"
                val resultsContent =
                    "${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} successes, ${result.failedTestCount} failures, ${result.skippedTestCount} skipped)"

                // Add padding
                val maxLength = resultsTitle.length.coerceAtLeast(resultsContent.length)
                val finalTitle = resultsTitle + " ".repeat(maxLength - resultsTitle.length)
                val finalContent = resultsContent + " ".repeat(maxLength - resultsContent.length)

                val startItem = "*  "
                val endItem = "  *"
                val repeatLength = startItem.length + maxLength + endItem.length
                val separator = "*".repeat(repeatLength)

                println("\n$separator")
                println("$startItem$finalTitle$endItem")
                println("$startItem$finalContent$endItem")
                println(separator)
            }
        }
    )
}
