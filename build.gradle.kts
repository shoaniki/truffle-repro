plugins {
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"


//-------
// Basic Gradle boilerplate.

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

val graalVersion = "21.1.0"

dependencies {
    implementation("org.graalvm.sdk:graal-sdk:$graalVersion")
}

application {
    mainModule.set("org.example.repro")
    mainClass.set("org.example.repro.Main")
}


//-------
// Add "-PwithReads" to the Gradle command line to enable the workaround.

if (project.hasProperty("withReads")) {
    application.applicationDefaultJvmArgs += listOf(
        "--add-reads", "org.graalvm.truffle=org.example.repro"
    )
}


//-------
// Handle the case where we're running in vanilla OpenJDK instead of GraalVM
// proper, by enabling JVMCI, installing the GraalVM compiler, and adding an
// explicit dependency on GraalVM JavaScript.

if (!System.getProperty("java.vendor").startsWith("GraalVM")) {
    dependencies.runtimeOnly("org.graalvm.js:js:$graalVersion")

    val upgradeModulePath: Configuration by configurations.creating
    dependencies {
        upgradeModulePath.isTransitive = false
        upgradeModulePath("org.graalvm.compiler:compiler:$graalVersion")
        upgradeModulePath("org.graalvm.compiler:compiler-management:$graalVersion")
    }

    application.applicationDefaultJvmArgs += listOf(
        "-XX:+UnlockExperimentalVMOptions", "-XX:+EnableJVMCI",
        "--upgrade-module-path", upgradeModulePath.resolve().joinToString(":")
    )
}
