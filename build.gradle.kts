// Copyright © 2020, Concordant and contributors.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
// associated documentation files (the "Software"), to deal in the Software without restriction,
// including without limitation the rights to use, copy, modify, merge, publish, distribute,
// sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or
// substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
// NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
// DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

description = "Concordant C-Client"
group = "concordant"
version = "1.1.4"

plugins {
    kotlin("multiplatform") version "1.4.20"
    kotlin("plugin.serialization") version "1.4.20"
    id("org.jetbrains.dokka") version "1.4.10.2"
    id("lt.petuska.npm.publish") version "1.0.2"
}

repositories {
    jcenter()
    mavenCentral()
    // for ts-generator
    maven(url = "https://jitpack.io")
    maven {
        url = uri("https://gitlab.inria.fr/api/v4/projects/18591/packages/maven")
        authentication {
            create<HttpHeaderAuthentication>("header")
        }
        // authentication by CI or private token
        credentials(HttpHeaderCredentials::class) {
            val CI_JOB_TOKEN = System.getenv("CI_JOB_TOKEN")
            if (CI_JOB_TOKEN == null){
                name = "Private-Token"
                val gitLabPrivateToken: String by project
                value = gitLabPrivateToken
            } else {
                name = "Job-Token"
                value = CI_JOB_TOKEN
            }
        }
    }
}

// Kotlin build config, per target
kotlin {
    // do not remove, even if empty
    jvm() {
        // uncomment if project contains Java source files
        //        withJava()
    }

    // Define "nodeJS" platform
    js("nodeJs") {
        // build for nodeJS
        nodejs {}
    }

    // Dependencies, per source set
    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }

        commonMain {
            dependencies {
                implementation("concordant:c-crdtlib:1.+")
                implementation("io.ktor:ktor-client-core:1.4.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0")
            }
        }

        commonTest {
            dependencies {
                implementation("io.kotest:kotest-property:4.3.1")
                implementation("io.kotest:kotest-assertions-core:4.3.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
                implementation("io.ktor:ktor-client-core:1.4.1")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio-jvm:1.4.1")
                implementation("com.github.ntrrgc:ts-generator:1.1.1")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5-jvm:4.3.1")
                implementation("io.ktor:ktor-client-cio-jvm:1.4.1")
            }
        }

        val nodeJsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:1.4.1")
            }
        }

        val nodeJsTest by getting {
            dependencies {
                implementation("io.kotest:kotest-framework-engine:4.3.1")
                implementation("io.ktor:ktor-client-js:1.4.1")
            }
        }
    }

    tasks {
        register<JavaExec>("tsgen") {
            group = "build"
            description = "Generate .d.ts description file"
            dependsOn("compileKotlinJvm")
            dependsOn("compileKotlinNodeJs")
            val mainClasses = kotlin.targets["jvm"].compilations["main"]
            classpath = configurations["jvmRuntimeClasspath"] + mainClasses.output.classesDirs
            main = "client.GenerateTSKt"
            outputs.file("$buildDir/js/packages/c-client-nodeJs/kotlin/c-client.d.ts")
        }
    }

}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

npmPublishing {
    organization = group as String
    readme = file("README.md")
    repositories {
        repository("Gitlab") {
            access = RESTRICTED
            registry = uri("https://gitlab.inria.fr/api/v4/projects/${System.getenv("CI_PROJECT_ID")}/packages/npm")
            authToken = System.getenv("CI_JOB_TOKEN")
        }
        repository("npmjs") {
            registry = uri("https://registry.npmjs.org")
            authToken = System.getenv("NPMJS_AUTH_TOKEN")
        }
    }
    publications {
        val nodeJs by getting {
            packageJson {
                types = "c-client.d.ts"
                "description" to project.description
                keywords = mutableListOf("concordant", "crdt", "conflict-free", "replicated datatypes")
                homepage = "concordant.io"
                license = "MIT"
                "bugs" to jsonObject {
                    "email" to "support@concordant.io"
                }
            }
        }
    }
}

// tasks dependencies
tasks {
    named("nodeJsMainClasses") {
        dependsOn("tsgen")
    }
}
