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

plugins {
    kotlin("multiplatform") version "1.4.10"
}

repositories {
    jcenter()
    mavenCentral()
}

kotlin {

    jvm() {
    }

    js("nodeJs") {
        nodejs {}
    }

    sourceSets {
        
        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }


        commonMain {
            dependencies {
                implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
            }
        }
        
        commonTest {
            dependencies {
                implementation("io.ktor:ktor-client-core:1.4.1")
                implementation("io.kotest:kotest-assertions-core:4.3.0")
                implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
            }
        }


        val jvmMain by getting {
            dependencies {
                implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio-jvm:1.4.1")
                implementation("io.kotest:kotest-runner-junit5-jvm:4.3.0")
                implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
            }
        }

        val nodeJsMain by getting {
            dependencies {
                implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
            }
        }

        val nodeJsTest by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:1.4.1")
                implementation("io.kotest:kotest-core-js:4.2.0.RC2")
                implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
            }
        }
    }
}

tasks.withType<Test> { useJUnitPlatform() }

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
