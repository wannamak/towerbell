plugins {
  kotlin("jvm") version "2.0.0"
  id("com.google.protobuf") version "0.9.4"
  java
  `java-test-fixtures`
}

group = "towerbell"
version = "1.5"

repositories {
  mavenCentral()
}

dependencies {
  implementation(fileTree("lib") { include("*.jar") })
  implementation(kotlin("stdlib"))
  testImplementation(kotlin("test"))
  testImplementation("org.junit.jupiter:junit-jupiter:6.0.3")
}

sourceSets {
  main {
    proto {
      srcDir("src/proto")
    }
  }
}

kotlin {
  jvmToolchain(21)
}

java {
  toolchain {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    languageVersion = JavaLanguageVersion.of(21)
  }
}

protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:3.25.6"
  }
}

tasks.test {
  useJUnitPlatform()

  jvmArgs = listOf(
    "-XX:+EnableDynamicAgentLoading",
    "-Xshare:off"
  )
}

tasks.register<Exec>("buildNative") {
  group = "build"
  description = "Compiles native files"

  commandLine("bash", "buildNative.sh")
  workingDir(projectDir)
}

tasks.register<Zip>("releaseZip") {
  group = "distribution"
  description = "Builds a release zip"

  dependsOn(tasks.jar)

  archiveFileName.set("${project.name}-${project.version}.zip")
  destinationDirectory.set(layout.buildDirectory.dir("dist"))

  from(tasks.jar.get().archiveFile) {
    into("lib")
  }

  from(projectDir) {
    include("LICENSE", "NOTICE")
  }

  from("lib") {
    into("lib")
  }

  from("src/www") {
    into("www")
  }

  from("libgpiod2") {
    include("libgpiod.so.3.1.2")
    into("lib")
  }

  from("build/native") {
    include("*.so")
    into("lib")
  }

  from("scripts") {
    include("towerbell.sh")
  }
}

// Order of task declaration and task references matters.  Keep this at the end.
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  dependsOn(tasks.named("generateProto"))
  dependsOn(tasks.named("buildNative"))
}
