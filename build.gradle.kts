import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.72"
    jacoco
    `maven-publish`
}

java {
    sourceCompatibility = JavaVersion.VERSION_13
    targetCompatibility = JavaVersion.VERSION_13
}

val moduleName = "com.github.asyncmc.template.internal"
val isSnapshot = version.toString().endsWith("SNAPSHOT")

repositories {
    jcenter()
    maven(url = "https://repo.gamemods.com.br/public/")
}

tasks.withType<JavaCompile>().configureEach {
    options.javaModuleVersion.set(provider { project.version as String })

    // this is needed because we have a separate compile step in this example with the 'module-info.java' is in 'main/java' and the Kotlin code is in 'main/kotlin'
    //options.compilerArgs = listOf("--patch-module", "$moduleName=${sourceSets.main.get().output.asPath}")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "13"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(":compileKotlin")
    inputs.property("moduleName", moduleName)
    doFirst {
        options.compilerArgs = options.compilerArgs + listOf(
            // include Gradle dependencies as modules
            "--module-path", sourceSets["main"].compileClasspath.asPath,
            "--patch-module", "$moduleName=${sourceSets.main.get().output.asPath}"
        )
        sourceSets["main"].compileClasspath = files()
    }
}

sourceSets.main.configure {
    //java.setSrcDirs(listOf("src/main/kotlin"))
}

plugins.withType<JavaPlugin>().configureEach {
    configure<JavaPluginExtension> {
        modularity.inferModulePath.set(true)
    }
}

dependencies {
    api(kotlin("stdlib-jdk8", embeddedKotlinVersion))
    api(kotlin("reflect", embeddedKotlinVersion))

    testImplementation(kotlin("test-junit5", embeddedKotlinVersion))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0-M1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0-M1")

    testImplementation("org.mockito:mockito-junit-jupiter:3.3.3")
    testImplementation("org.mockito:mockito-inline:3.3.3")
    testImplementation("com.nhaarman:mockito-kotlin:1.6.0")

    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("com.natpryce:hamkrest:1.7.0.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging.showStandardStreams = true
    testLogging {
        events("PASSED", "FAILED", "SKIPPED", "STANDARD_OUT", "STANDARD_ERROR")
    }
}

jacoco {
    //toolVersion = jacocoVersion
    reportsDir = file("$buildDir/reports/jacoco")
}

tasks {
    named<JacocoReport>("jacocoTestReport") {
        dependsOn("test")
        classDirectories.setFrom(files("${buildDir}/classes"))
        reports {
            xml.isEnabled = true
            html.isEnabled = true
        }
    }


    create<Jar>("sourceJar") {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    withType<Jar>().configureEach {
        from(projectDir) {
            include("LICENSE.txt")
            include("NOTICE.md")
        }
    }
}


fun findProp(name: String) = findProperty(name)?.toString()?.takeIf { it.isNotBlank() }
    ?: System.getenv(name.replace('.', '_').toUpperCase())?.takeIf { it.isNotBlank() }

publishing {
    repositories {
        maven {
            val prefix = if (isSnapshot) "asyncmc.repo.snapshot" else "asyncmc.repo.release"
            url = uri(findProp("$prefix.url") ?: "$buildDir/repo")
            when(findProp("$prefix.auth.type")) {
                "password" -> credentials {
                    username = findProp("$prefix.auth.username")
                    password = findProp("$prefix.auth.password")
                }
                "aws" -> credentials(AwsCredentials::class.java) {
                    accessKey = findProp("$prefix.auth.access_key")
                    secretKey = findProp("$prefix.auth.secret_key")
                    sessionToken = findProp("$prefix.auth.session_token")
                }
                "header" -> credentials(HttpHeaderCredentials::class.java) {
                    name = findProp("$prefix.auth.header_name")
                    value = findProp("$prefix.auth.header_value")
                }
            }
        }
    }

    publications {
        create<MavenPublication>("library") {
            from(components["java"])
            artifact(tasks["sourceJar"])
            pom {
                name.set("Internal Template")
                description.set("This is just a template project")
                url.set("https://github.com/AsyncMC/Internal-Template")
                licenses {
                    license {
                        name.set("Public domain")
                        url.set("https://github.com/AsyncMC/Internal-Template/LICENSE.txt")
                    }
                }
                developers {
                    developer {
                        id.set("joserobjr")
                        name.set("Jos?? Roberto de Ara??jo J??nior")
                        email.set("joserobjr@gamemods.com.br")
                    }
                }
                scm {
                    url.set("https://github.com/AsyncMC/Internal-Template")
                    connection.set("scm:git:https://github.com/AsyncMC/Internal-Template.git")
                    developerConnection.set("https://github.com/AsyncMC/Internal-Template.git")
                }
            }
        }
    }
}
