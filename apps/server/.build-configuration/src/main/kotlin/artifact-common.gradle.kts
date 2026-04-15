plugins {
    id("internal.common")
}

configurations.all {
    withDependencies {
        val projectDependencies = this.filterIsInstance<ProjectDependency>()
        projectDependencies.forEach {
            val dependentProjectPath = it.path
            if (dependentProjectPath.endsWith("-admin")) {
                logger.error("Only the executable modules are allowed to declare dependencies on *-admin modules")
                throw GradleException("Only the executable modules are allowed to declare dependencies on *-admin modules")
            }

            if (!name.startsWith("test") && dependentProjectPath.endsWith("-test")) {
                logger.error("*-test modules can only be included via a test-configuration they cannot be included in the production code")
                throw GradleException("*-test modules can only be included via a test-configuration they cannot be included in the production code")
            }
        }
    }
}