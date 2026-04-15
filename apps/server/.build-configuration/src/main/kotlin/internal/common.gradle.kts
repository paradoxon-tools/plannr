package internal

import extensions.config
import extensions.ifPresent
import java.util.concurrent.TimeUnit.SECONDS

plugins {
    id("internal.kotlin")
    id("internal.libs")
    id("internal.spring")
    id("internal.testing")
}

dependencies {
    config.versions.stdlib.ifPresent {
        implementation("com.dericon.stdlib:stdlib:$it")
        testImplementation("com.dericon.stdlib:stdlib-test:$it")
    }
}

configurations.all {
    resolutionStrategy.cacheDynamicVersionsFor(0, SECONDS)
}
