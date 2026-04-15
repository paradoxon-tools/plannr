import org.gradle.jvm.tasks.Jar

plugins {
    id("java")
    id("artifact")
}

val testArtifacts by configurations.creating {
    extendsFrom(configurations["testRuntimeClasspath"])
}

tasks.register<Jar>("testJar") {
    dependsOn("testClasses")
    archiveClassifier.set("tests")
    from(sourceSets.test.get().output)
}

artifacts {
    add("testArtifacts", tasks["testJar"])
}