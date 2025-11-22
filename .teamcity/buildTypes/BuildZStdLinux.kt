package buildTypes

import LinuxArch
import appendBashMultiline
import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.DslContext
import jetbrains.buildServer.configs.kotlin.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import linux
import vcsRoots.ZStd

class BuildZStdLinux(val archs: Collection<LinuxArch>) : BuildType({
    name = "Build ZStd Linux"
    id("BuildZStdLinux")

    vcs {
        root(ZStd, "+:.")
    }

    val dockerTag = "ubuntu_with_cross_compilers:local"


    val outDir = "out"

    artifactRules = """
        $outDir/**/*
    """.trimIndent()

    vcs {
        root(DslContext.settingsRoot, """
            +:.
            -:.teamcity
        """.trimIndent())
    }

    steps {
        dockerCommand {
            name = "Prepare docker with cross-compilers"
            this.commandType = build {
                val deps = archs.flatMap { it.dependencies }
                source = content {
                    this.content = buildString {
                        appendLine("FROM ubuntu:24.04")
                        appendLine()

                        appendBashMultiline(
                            "RUN apt update &&",
                            "apt install -y --no-install-recommends",
                            *deps.toTypedArray(),
                        )
                    }
                }

                this.namesAndTags = dockerTag
            }
        }
        archs.forEach { arch ->

            script {
                name = "Build ${arch.humanReadableName}"
                scriptContent = """
                make clean
                ${arch.compilerEnvString} make
                
                mkdir -p out
                cp programs/zstd out/${arch.os}/${arch.architecture}/${arch.filename}
            """.trimIndent()
                dockerImage = dockerTag
            }

        }
    }

    requirements {
        linux()
    }
}) {
}