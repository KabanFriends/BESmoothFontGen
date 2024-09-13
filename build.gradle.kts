import org.beryx.runtime.util.JdkUtil.JdkDownloadOptions

plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.1"
    id("org.beryx.runtime") version "1.13.1"
}

group = "io.github.kabanfriends"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("org.apache.pdfbox:fontbox:3.0.3")
    implementation("commons-io:commons-io:2.16.1")
}

application {
    mainClass.set("io.github.kabanfriends.smoothfontgen.SmoothFontGen")
}

runtime {
    targetPlatform("linux-x64") {
        jdkHome = jdkDownload("https://github.com/AdoptOpenJDK/openjdk17-binaries/releases/download/jdk-2021-05-07-13-31/OpenJDK-jdk_x64_linux_hotspot_2021-05-06-23-30.tar.gz")
    }
    targetPlatform("windows-x64") {
        jdkHome = jdkDownload("https://github.com/AdoptOpenJDK/openjdk17-binaries/releases/download/jdk-2021-05-07-13-31/OpenJDK-jdk_x64_windows_hotspot_2021-05-06-23-30.zip")
    }

    launcher {
        unixScriptTemplate = file("unixScriptTemplate.txt")
        windowsScriptTemplate = file("windowsScriptTemplate.txt")
    }

    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    modules.set(listOf("java.desktop"))
}

System.setProperty("RUNTIME_EXECUTABLE_NAME", "${base.archivesName.get()}-${version}-all.jar")

tasks.register("createDist") {
    dependsOn("runtime")

    doLast {
        file("dist").deleteRecursively()
        file("build/jre/").listFiles()?.filter { it.isDirectory }?.forEach { dir ->
            copy {
                from(dir)
                into(file("dist/${dir.name}/jre"))
            }
            file("build/install/${project.name}-shadow/bin").listFiles()?.filter { it.isFile && it.name.startsWith(project.name) }?.forEach {file ->
                copy {
                    from(file)
                    into(file("dist/${dir.name}"))
                }
            }
            copy {
                from(file("build/install/${project.name}-shadow/lib"))
                into(file("dist/${dir.name}"))
            }
        }
    }
}